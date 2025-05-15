package com.taskflow.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateTaskRequest {
    private String name;
    private Integer priority;
    private List<Long> dependencies; // 可选的依赖任务 ID 列表
    private Integer duration;   // 执行耗时
    private Double failRate;    // 失败概率
}

