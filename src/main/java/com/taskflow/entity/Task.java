package com.taskflow.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tasks")
public class Task {
    @TableId
    private Long id;

    private String name;
    private Integer priority;
    private String status;

    private LocalDateTime createdTime;
    private LocalDateTime scheduledTime;
    private LocalDateTime executedTime;
}

