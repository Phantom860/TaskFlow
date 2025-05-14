package com.taskflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("tasks")
public class Task implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long taskId;

    private String name;
    private Integer priority;
    private String status;

    private LocalDateTime createTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Integer retryCount;
}

