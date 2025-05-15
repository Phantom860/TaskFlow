package com.taskflow.entity;

public enum TaskStatus {
    WAITING,     // 等待执行（初始状态）
    RUNNING,     // 正在执行
    SUCCESS,     // 成功完成
    FAILED,      // 执行失败
    CANCELED    // 被取消
}
