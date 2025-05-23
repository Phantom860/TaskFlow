package com.taskflow.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taskflow.entity.Task;
import com.taskflow.entity.TaskStatus;
import com.taskflow.mapper.TaskDependencyMapper;
import com.taskflow.mapper.TaskMapper;
import com.taskflow.service.TaskLogService;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Executor;

@Component
public class TaskDispatcher {

    @Resource
    private TaskMapper taskMapper;

    @Resource
    private TaskDependencyMapper dependencyMapper;

    @Resource
    private TaskLogService taskLogService;

    @Resource(name = "taskExecutor")
    private Executor taskExecutor;

    @Scheduled(fixedDelay = 2000)
    public void scheduleLoop() {
        List<Task> waitingTasks = taskMapper.selectList(
                new QueryWrapper<Task>().eq("status", TaskStatus.WAITING.name())
        );

        // 按优先级从高到低排序（使用优先队列）
        PriorityQueue<Task> queue = new PriorityQueue<>(
                (a, b) -> Integer.compare(b.getPriority(), a.getPriority())
        );
        queue.addAll(waitingTasks);

        while (!queue.isEmpty()) {
            Task task = queue.poll();
            if (canRun(task)) {
                submitTaskAsync(task);
            }
        }
    }

    private boolean canRun(Task task) {
        return dependencyMapper.countUnfinishedDependencies(task.getTaskId()) == 0;
    }

    private void submitTaskAsync(Task task) {
        taskExecutor.execute(() -> {
            try {
                // 第一次检查：任务可能在调度之前就被取消了
                Task latest = taskMapper.selectById(task.getTaskId());
                if (!TaskStatus.WAITING.name().equals(latest.getStatus())) {
                    System.out.println("任务 " + task.getTaskId() + " 已取消或已处理，跳过调度");
                    taskLogService.saveLog(task.getTaskId(), "任务已取消或已处理，跳过调度");
                    return;
                }

                System.out.println("开始执行任务：" + task.getName());

                task.setStatus(TaskStatus.RUNNING.name());
                task.setStartTime(LocalDateTime.now());
                taskMapper.updateById(task);
                taskLogService.saveLog(task.getTaskId(), "任务开始执行");

                // 模拟执行过程（分成若干小段，中途检查是否被取消）
                int totalMillis = task.getDuration() != null ? task.getDuration() : 10000;
                int interval = totalMillis / 100;
                for (int elapsed = 0; elapsed < totalMillis; elapsed += interval) {
                    Thread.sleep(interval);

                    // 中途检查任务是否被取消
                    Task check = taskMapper.selectById(task.getTaskId());
                    if (TaskStatus.CANCELED.name().equals(check.getStatus())) {
                        System.out.println("任务已被取消：" + task.getName());
                        taskLogService.saveLog(task.getTaskId(), "任务执行中被取消");
                        return;
                    }

                    // 每次循环更新任务进度
                    int progress = Math.min((elapsed + interval) * 100 / totalMillis, 100);
                    task.setProgress(progress);
                    taskMapper.updateById(task);
                }

                // 模拟失败：默认 30% 概率
                double failRate = task.getFailRate() != null ? task.getFailRate() : 0.3;
                if (Math.random() < failRate) {
                    throw new RuntimeException("模拟失败");
                }

                // 执行成功
                task.setStatus(TaskStatus.SUCCESS.name());
                task.setProgress(100); // 成功设为100%
                task.setEndTime(LocalDateTime.now());
                taskMapper.updateById(task);
                System.out.println("任务成功：" + task.getName());
                taskLogService.saveLog(task.getTaskId(), "任务执行成功");

            } catch (Exception e) {
                task.setStatus(TaskStatus.FAILED.name());
                task.setEndTime(LocalDateTime.now());
                taskMapper.updateById(task);
                System.err.println("任务失败：" + task.getName());
                task.setProgress(0); // 失败设为0%
                taskLogService.saveLog(task.getTaskId(), "任务执行失败：" + e.getMessage());
                e.printStackTrace();
            }
        });
    }



}
