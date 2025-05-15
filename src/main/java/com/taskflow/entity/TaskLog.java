package com.taskflow.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("task_logs")
public class TaskLog implements Serializable {
    @TableId(value = "id")
    private Long logId;

    private Long taskId;
    private String message;
    private LocalDateTime time;
}

