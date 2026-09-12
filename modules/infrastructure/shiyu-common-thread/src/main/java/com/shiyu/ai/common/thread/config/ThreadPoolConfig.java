package com.shiyu.ai.common.thread.config;

import com.shiyu.ai.common.core.utils.Threads;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/** 线程池配置 */
@Configuration
@EnableConfigurationProperties(ThreadPoolProperties.class)
public class ThreadPoolConfig {

    /**
     * {@code threadPoolTaskExecutor} 执行当前类型定义的业务操作。
     *
     * @param threadPoolProperties 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean(name = "threadPoolTaskExecutor")
    @ConditionalOnProperty(prefix = "thread-pool", name = "enabled", havingValue = "true")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor(
            ThreadPoolProperties threadPoolProperties) {
        int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(
                threadPoolProperties.getCorePoolSize() > 0
                        ? threadPoolProperties.getCorePoolSize()
                        : cores + 1);
        executor.setMaxPoolSize(
                threadPoolProperties.getMaxPoolSize() > 0
                        ? threadPoolProperties.getMaxPoolSize()
                        : (cores + 1) * 2);
        executor.setQueueCapacity(threadPoolProperties.getQueueCapacity());
        executor.setKeepAliveSeconds(threadPoolProperties.getKeepAliveSeconds());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    /** 执行周期性或定时任务 */
    @Bean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService(
            ThreadPoolProperties threadPoolProperties) {
        int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
        int scheduledCoreSize =
                threadPoolProperties.getScheduledCorePoolSize() > 0
                        ? threadPoolProperties.getScheduledCorePoolSize()
                        : cores + 1;
        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory("schedule-pool-");
        threadFactory.setDaemon(true);
        return new ScheduledThreadPoolExecutor(
                scheduledCoreSize, threadFactory, new ThreadPoolExecutor.CallerRunsPolicy()) {
            /**
             * {@code afterExecute} 执行当前类型定义的业务操作。
             *
             * @param r 参数值，用于执行当前操作。
             * @param t 参数值，用于执行当前操作。
             */
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                Threads.printException(r, t);
            }
        };
    }
}
