package com.taskflow.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("task_dependencies")
public class TaskDependency implements Serializable {
    @TableId
    private Long id;
    private Long taskId;
    private Long dependsOn;
}

