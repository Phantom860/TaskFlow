package com.taskflow.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_logs")
public class TaskLog {
    @TableId
    private Long id;

    private Long taskId;
    private String message;
    private LocalDateTime time;
}

