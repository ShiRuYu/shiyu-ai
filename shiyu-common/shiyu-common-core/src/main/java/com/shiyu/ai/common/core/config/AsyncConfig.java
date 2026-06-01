package com.shiyu.ai.common.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Arrays;
import java.util.concurrent.Executor;

/**
 * 异步配置
 */
@Slf4j
@EnableAsync(proxyTargetClass = true)
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    /**
     * 自定义 @Async 注解使用系统线程池
     * 留空则使用 Spring Boot 自动配置的 TaskExecutor
     * (Spring Boot 4 支持虚拟线程: spring.threads.virtual.enabled=true)
     */
    @Override
    public Executor getAsyncExecutor() {
        return null;
    }

    /**
     * 异步执行异常处理
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) ->
            log.error("Async method [{}] with params {} failed", method.getName(), Arrays.toString(objects), throwable);
    }

}
