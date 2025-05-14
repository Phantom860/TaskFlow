package com.taskflow.service;

import com.taskflow.dto.CreateTaskRequest;
import com.taskflow.entity.Task;
import java.util.List;

public interface TaskService {
    List<Task> getAllTasks();

    void createTask(CreateTaskRequest request);

    boolean deleteTaskById(Long id);
}

