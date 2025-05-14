package com.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taskflow.dto.CreateTaskRequest;
import com.taskflow.dto.Result;
import com.taskflow.entity.Task;
import com.taskflow.entity.TaskDependency;
import com.taskflow.mapper.TaskDependencyMapper;
import com.taskflow.mapper.TaskMapper;
import com.taskflow.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskDependencyMapper dependencyMapper;

    @Override
    public List<Task> getAllTasks() {
        // 查询所有任务（不加条件）
        return taskMapper.selectList(new QueryWrapper<>());
    }

    @Override
    public void createTask(CreateTaskRequest request) {
        // 1. 创建任务对象
        Task task = new Task();
        task.setName(request.getName());
        task.setPriority(request.getPriority());
        task.setStatus("WAITING"); // 初始状态
        task.setCreateTime(LocalDateTime.now());
        task.setRetryCount(0);

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

    @Override
    public boolean deleteTaskById(Long id) {
        // 先删除与该任务相关的依赖关系
        dependencyMapper.delete(new QueryWrapper<TaskDependency>().eq("task_id", id));
        dependencyMapper.delete(new QueryWrapper<TaskDependency>().eq("depends_on", id));

        // 删除任务本身
        int deleted = taskMapper.deleteById(id);
        return deleted > 0;
    }

    @Override
    public Result listTasks(int page, int size) {
        Page<Task> pageInfo = new Page<>(page, size);
        Page<Task> resultPage = taskMapper.selectPage(pageInfo, null);
        return Result.ok(resultPage.getRecords(), resultPage.getTotal());
    }
}
