
package com.shiyu.ai.common.thread.core;

import com.shiyu.ai.common.thread.api.PoolType;
import com.shiyu.ai.common.thread.api.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 默认线程池管理器实现
 * 提供线程池的创建、获取和管理功能
 */
public class DefaultThreadPoolManager implements ThreadPoolManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultThreadPoolManager.class);

    /**
     * 线程池存储映射
     */
    private final Map<String, Executor> executorMap = new ConcurrentHashMap<>();

    /**
     * 线程池工厂
     */
    private final ExecutorFactory executorFactory;

    public DefaultThreadPoolManager() {
        this(new DefaultExecutorFactory());
    }

    public DefaultThreadPoolManager(ExecutorFactory executorFactory) {
        this.executorFactory = executorFactory;
        // 初始化默认线程池
        initializeDefaultExecutors();
    }

    /**
     * 初始化默认线程池
     */
    private void initializeDefaultExecutors() {
        // 创建默认线程池
        getExecutor(PoolType.DEFAULT);
        // 如果Java版本支持，创建虚拟线程池
        if (VirtualExecutorFactory.isSupported()) {
            getExecutor(PoolType.VIRTUAL);
        }
    }

    @Override
    public Executor getExecutor(PoolType poolType) {
        return getExecutor(poolType, poolType.getCode());
    }

    @Override
    public Executor getExecutor(PoolType poolType, String name) {
        String key = poolType.getCode() + ":" + name;
        return executorMap.computeIfAbsent(key, k -> {
            Executor executor = executorFactory.createExecutor(poolType, name);
            logger.info("创建线程池: {}, 类型: {}", name, poolType.getDescription());
            return executor;
        });
    }

    @Override
    public Executor getDefaultExecutor() {
        return getExecutor(PoolType.DEFAULT);
    }

    /**
     * 关闭所有线程池
     */
    public void shutdownAll() {
        executorMap.forEach((name, executor) -> {
            try {
                if (executor instanceof ExecutorService) {
                    ExecutorService service = (ExecutorService) executor;
                    service.shutdown();
                    if (!service.awaitTermination(30, TimeUnit.SECONDS)) {
                        logger.warn("线程池 {} 在30秒内未能正常关闭，强制关闭", name);
                        service.shutdownNow();
                    }
                    logger.info("线程池 {} 已关闭", name);
                }
            } catch (InterruptedException e) {
                logger.error("关闭线程池 {} 时被中断", name, e);
                Thread.currentThread().interrupt();
            }
        });
        executorMap.clear();
    }

    /**
     * 获取线程池信息
     * 
     * @param name 线程池名称
     * @return 线程池信息
     */
    public String getPoolInfo(String name) {
        Executor executor = executorMap.get(name);
        if (executor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor pool = (ThreadPoolExecutor) executor;
            return String.format("线程池信息: [核心线程数: %d, 最大线程数: %d, 活跃线程数: %d, 队列大小: %d, 已完成任务数: %d]",
                    pool.getCorePoolSize(),
                    pool.getMaximumPoolSize(),
                    pool.getActiveCount(),
                    pool.getQueue().size(),
                    pool.getCompletedTaskCount());
        }
        return "线程池信息不可用";
    }
}
