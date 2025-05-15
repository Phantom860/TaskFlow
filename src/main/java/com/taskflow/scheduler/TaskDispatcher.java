package com.taskflow.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taskflow.entity.Task;
import com.taskflow.entity.TaskStatus;
import com.taskflow.mapper.TaskDependencyMapper;
import com.taskflow.mapper.TaskMapper;
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
                    return;
                }

                System.out.println("开始执行任务：" + task.getName());

                task.setStatus(TaskStatus.RUNNING.name());
                task.setStartTime(LocalDateTime.now());
                taskMapper.updateById(task);

                // 模拟执行过程（分成若干小段，中途检查是否被取消）
                int totalMillis = 1000;
                int interval = 1000; // 每隔1秒检查一次
                for (int elapsed = 0; elapsed < totalMillis; elapsed += interval) {
                    Thread.sleep(interval);

                    // 中途检查任务是否被取消
                    Task check = taskMapper.selectById(task.getTaskId());
                    if (TaskStatus.CANCELED.name().equals(check.getStatus())) {
                        System.out.println("任务已被取消：" + task.getName());
                        return;
                    }
                }

                // 模拟失败：30% 概率
                if (Math.random() < 0.3) {
                    throw new RuntimeException("模拟失败");
                }

                // 执行成功
                task.setStatus(TaskStatus.SUCCESS.name());
                task.setEndTime(LocalDateTime.now());
                taskMapper.updateById(task);
                System.out.println("任务成功：" + task.getName());

            } catch (Exception e) {
                task.setStatus(TaskStatus.FAILED.name());
                task.setEndTime(LocalDateTime.now());
                taskMapper.updateById(task);
                System.err.println("任务失败：" + task.getName());
                e.printStackTrace();
            }
        });
    }

}
