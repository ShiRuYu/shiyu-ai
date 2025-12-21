
package com.shiyu.ai.common.thread.core;

import com.shiyu.ai.common.thread.api.PoolType;
import com.shiyu.ai.common.thread.api.TaskDecorator;
import com.shiyu.ai.common.thread.api.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 默认线程池管理器实现
 * 提供线程池的创建、获取和管理功能
 */
public class DefaultThreadPoolManager implements ThreadPoolManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultThreadPoolManager.class);

    /**
     * 线程池存储映射
     */
    private final Map<String, ExecutorService> executorMap = new ConcurrentHashMap<>();

    /**
     * 线程池工厂
     */
    private final ExecutorFactory executorFactory;

    private final TaskDecorator taskDecorator;

    public DefaultThreadPoolManager(TaskDecorator taskDecorator) {
        this(new DefaultExecutorFactory(), taskDecorator);
    }

    public DefaultThreadPoolManager(ExecutorFactory executorFactory, TaskDecorator taskDecorator) {
        this.executorFactory = executorFactory;
        this.taskDecorator = taskDecorator;
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
    public ExecutorService getExecutor(PoolType poolType) {
        return getExecutor(poolType, poolType.getCode());
    }

    @Override
    public ExecutorService getExecutor(PoolType poolType, String name) {
        String key = poolType.getCode() + ":" + name;
        ExecutorService executor1 = executorMap.computeIfAbsent(key, k -> {
            ExecutorService executor = executorFactory.createExecutor(poolType, name);
            logger.info("创建线程池: {}, 类型: {}", name, poolType.getDescription());
            return executor;
        });
        return new SafeExecutorService(executor1, taskDecorator);
    }

    @Override
    public ExecutorService getDefaultExecutor() {
        return getExecutor(PoolType.DEFAULT);
    }

    /**
     * 关闭所有线程池
     */
    public void shutdownAll() {
        executorMap.forEach((name, executor) -> {
            try {
                if (executor instanceof ExecutorService service) {
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
        ExecutorService executor = executorMap.get(name);
        if (executor instanceof ThreadPoolExecutor pool) {
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
