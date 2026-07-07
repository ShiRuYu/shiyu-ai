package com.shiyu.ai.common.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;

/**
 * 异步配置
 * 使用 @Bean 方式提供 AsyncTaskExecutor，避免 AsyncConfigurer 接口冲突
 */
@Slf4j
@EnableAsync(proxyTargetClass = true)
@Configuration
public class AsyncConfig {

    /**
     * 自定义 @Async 注解使用的线程池
     */
    @Bean
    public AsyncTaskExecutor applicationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * 异步执行异常处理
     */
    @Bean
    public AsyncUncaughtExceptionHandler asyncUncaughtExceptionHandler() {
        return (throwable, method, objects) ->
            log.error("Async method [{}] with params {} failed", method.getName(), Arrays.toString(objects), throwable);
    }
}
