package com.taskflow.service;

import com.taskflow.entity.TaskLog;

import java.util.List;

public interface TaskLogService {
    List<TaskLog> getLogsByTaskId(Long taskId);

    void saveLog(Long taskId, String message);
}
