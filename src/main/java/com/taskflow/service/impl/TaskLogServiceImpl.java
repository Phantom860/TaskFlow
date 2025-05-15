package com.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taskflow.entity.TaskLog;
import com.taskflow.mapper.TaskLogMapper;
import com.taskflow.service.TaskLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskLogServiceImpl implements TaskLogService {

    @Resource
    private TaskLogMapper taskLogMapper;

    public List<TaskLog> getLogsByTaskId(Long taskId) {
        return taskLogMapper.selectList(
                new QueryWrapper<TaskLog>()
                        .eq("task_id", taskId)
                        .orderByAsc("time")
        );
    }

    public void saveLog(Long taskId, String message) {
        TaskLog log = new TaskLog();
        log.setTaskId(taskId);
        log.setMessage(message);
        log.setTime(LocalDateTime.now());
        taskLogMapper.insert(log);
    }
}
