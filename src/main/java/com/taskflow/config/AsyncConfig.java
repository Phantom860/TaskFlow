package com.taskflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);     // 最小线程数
        executor.setMaxPoolSize(8);      // 最大线程数
        executor.setQueueCapacity(100);  // 队列容量
        executor.setThreadNamePrefix("TaskRunner-");
        executor.initialize();
        return executor;
    }
}

