package com.taskflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("task_dependencies")
public class TaskDependency {
    private Long taskId;
    private Long dependsOn;
}

