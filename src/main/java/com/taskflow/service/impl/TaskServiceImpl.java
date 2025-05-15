package com.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taskflow.dto.CreateTaskRequest;
import com.taskflow.dto.Result;
import com.taskflow.entity.Task;
import com.taskflow.entity.TaskDependency;
import com.taskflow.entity.TaskStatus;
import com.taskflow.mapper.TaskDependencyMapper;
import com.taskflow.mapper.TaskLogMapper;
import com.taskflow.mapper.TaskMapper;
import com.taskflow.service.TaskLogService;
import com.taskflow.service.TaskService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    @Resource
    private TaskMapper taskMapper;

    @Resource
    private TaskDependencyMapper dependencyMapper;

    @Resource
    private TaskLogService taskLogService;

    @Resource
    private TaskLogMapper taskLogMapper;

    @Override
    public List<Task> getAllTasks() {
        // 查询所有任务（不加条件）
        return taskMapper.selectList(new QueryWrapper<>());
    }

    /**
     * 创建任务
     *
     * @param request 任务创建请求
     */
    @Override
    public void createTask(CreateTaskRequest request) {
        // 1. 创建任务对象
        Task task = new Task();
        task.setName(request.getName());
        task.setPriority(request.getPriority());
        task.setStatus(TaskStatus.WAITING.name()); // 初始状态
        task.setCreateTime(LocalDateTime.now());
        task.setRetryCount(0);
        task.setDuration(request.getDuration() != null ? request.getDuration() : 10000);
        task.setFailRate(request.getFailRate() != null ? request.getFailRate() : 0.3);

        // 2. 插入任务
        taskMapper.insert(task);

        // 3. 插入依赖关系（如果有）
        if (request.getDependencies() != null) {
            for (Long depId : request.getDependencies()) {
                TaskDependency dependency = new TaskDependency();
                dependency.setTaskId(task.getTaskId()); // 获取插入后生成的任务 ID
                dependency.setDependsOn(depId);
                dependencyMapper.insert(dependency);
            }
        }
    }

    /**
     * 删除任务
     *
     * @param id 任务 ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteTaskById(Long id) {
        // 先删除与该任务相关的依赖关系
        dependencyMapper.delete(new QueryWrapper<TaskDependency>().eq("task_id", id));
        dependencyMapper.delete(new QueryWrapper<TaskDependency>().eq("depends_on", id));

        // 删除任务本身
        int deleted = taskMapper.deleteById(id);
        return deleted > 0;
    }

    /**
     * 列出所有任务
     *
     * @param page 页码
     * @param size 每页大小
     * @return 任务列表
     */
    @Override
    public Result listTasks(int page, int size) {
        Page<Task> pageInfo = new Page<>(page, size);
        Page<Task> resultPage = taskMapper.selectPage(pageInfo, null);
        return Result.ok(resultPage.getRecords(), resultPage.getTotal());
    }

    /**
     * 取消任务
     *
     * @param taskId 任务 ID
     * @return 是否取消成功
     */
    @Override
    public boolean cancelTask(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) return false;

        if (TaskStatus.WAITING.name().equals(task.getStatus()) || TaskStatus.RUNNING.name().equals(task.getStatus())) {
            task.setStatus(TaskStatus.CANCELED.name());
            task.setEndTime(LocalDateTime.now());
            taskMapper.updateById(task);
            return true;
        }
        return false;
    }

    /**
     * 重试任务
     *
     * @param taskId 任务 ID
     * @return 重试结果
     */
    @Override
    public String retryTask(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            return "TASK_NOT_FOUND";
        }

        if (TaskStatus.SUCCESS.name().equals(task.getStatus())) {
            taskLogService.saveLog(taskId, "任务已成功，无需重试");
            return "ALREADY_SUCCESS";

        }

        int retryCount = task.getRetryCount() != null ? task.getRetryCount() : 0;
        if (retryCount >= 3) {
            taskLogService.saveLog(taskId, "超过最大重试次数，任务彻底失败");
            return "RETRY_LIMIT_REACHED";
        }

        task.setRetryCount(retryCount + 1);
        task.setStartTime(LocalDateTime.now());

        //30%概率成功
        boolean isSuccess = Math.random() < 0.3;

        if (isSuccess) {
            task.setStatus(TaskStatus.SUCCESS.name());
            task.setEndTime(LocalDateTime.now());
            taskMapper.updateById(task);
            taskLogService.saveLog(taskId, "用户点击重试：第 " + (retryCount + 1) + " 次成功");
            return "RETRY_SUCCESS";
        } else {
            task.setStatus(TaskStatus.FAILED.name());
            task.setEndTime(LocalDateTime.now());
            taskMapper.updateById(task);
            taskLogService.saveLog(taskId, "用户点击重试：第 " + (retryCount + 1) + " 次失败");
            return retryCount + 1 >= 3 ? "RETRY_LIMIT_REACHED" : "RETRY_FAILED";
        }
    }

    @Override
    public boolean clearAllTasks() {
        try {
            taskMapper.delete(null);
            taskLogMapper.delete(null);
            dependencyMapper.delete(null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
