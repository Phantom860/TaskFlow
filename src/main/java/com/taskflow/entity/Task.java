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
    private Integer progress; // 任务进度百分比 (0~100)
    private Integer duration;   // 耗时，单位毫秒（默认 10000）
    private Double failRate;    // 失败概率（默认 0.3）
}

