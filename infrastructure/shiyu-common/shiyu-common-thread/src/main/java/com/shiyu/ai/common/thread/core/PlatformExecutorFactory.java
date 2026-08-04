
package com.shiyu.ai.common.thread.core;

import com.shiyu.ai.common.thread.api.PoolType;
import com.shiyu.ai.common.thread.config.ThreadingProperties;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 平台线程池工厂
 * 创建基于平台线程的线程池
 */
public class PlatformExecutorFactory implements ExecutorFactory {

    private final ThreadingProperties properties;

    public PlatformExecutorFactory() {
        this(null);
    }

    public PlatformExecutorFactory(ThreadingProperties properties) {
        this.properties = properties;
    }

    @Override
    public ExecutorService createExecutor(PoolType poolType, String name) {
        switch (poolType) {
            case DEFAULT:
                return createDefaultExecutor(name);
            case CPU_INTENSIVE:
                return createCpuIntensiveExecutor(name);
            case IO_INTENSIVE:
                return createIoIntensiveExecutor(name);
            case SCHEDULED:
                return createScheduledExecutor(name);
            case PRIORITY:
                return createPriorityExecutor(name);
            case VIRTUAL:
                // 平台线程工厂不支持虚拟线程，使用默认实现
                return createDefaultExecutor(name);
            case CUSTOM:
            default:
                return createDefaultExecutor(name);
        }
    }

    /**
     * 创建默认线程池
     * 
     * @param name 线程池名称
     * @return 线程池执行器
     */
    private ExecutorService createDefaultExecutor(String name) {
        int corePoolSize = coreSize(name, Math.max(2, Runtime.getRuntime().availableProcessors() / 2));
        int maximumPoolSize = maxSize(name, Math.max(corePoolSize, Runtime.getRuntime().availableProcessors()));
        long keepAliveTime = keepAlive(name);

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity(name, 100)),
                new NamedThreadFactory(threadName(name)),
                rejectionHandler(name)
        );
        executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut(name));
        return executor;
    }

    /**
     * 创建CPU密集型任务线程池
     * 
     * @param name 线程池名称
     * @return 线程池执行器
     */
    private ExecutorService createCpuIntensiveExecutor(String name) {
        int corePoolSize = coreSize(name, Runtime.getRuntime().availableProcessors());
        int maximumPoolSize = maxSize(name, corePoolSize);

        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueCapacity(name, 50)),
                new NamedThreadFactory(threadName(name) + "-cpu"),
                rejectionHandler(name)
        );
    }

    /**
     * 创建IO密集型任务线程池
     * 
     * @param name 线程池名称
     * @return 线程池执行器
     */
    private ExecutorService createIoIntensiveExecutor(String name) {
        int corePoolSize = coreSize(name, Runtime.getRuntime().availableProcessors());
        int maximumPoolSize = maxSize(name, corePoolSize * 2);
        long keepAliveTime = keepAlive(name);

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity(name, 200)),
                new NamedThreadFactory(threadName(name) + "-io"),
                rejectionHandler(name)
        );
        executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut(name));
        return executor;
    }

    /**
     * 创建调度任务线程池
     * 
     * @param name 线程池名称
     * @return 调度线程池执行器
     */
    private ScheduledThreadPoolExecutor createScheduledExecutor(String name) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
                coreSize(name, Math.max(2, Runtime.getRuntime().availableProcessors() / 2)),
                new NamedThreadFactory(threadName(name) + "-scheduled")
        );
        // 设置移除已取消任务的策略
        executor.setRemoveOnCancelPolicy(true);
        executor.setRejectedExecutionHandler(rejectionHandler(name));
        return executor;
    }

    /**
     * 创建优先级任务线程池
     * 
     * @param name 线程池名称
     * @return 线程池执行器
     */
    private ExecutorService createPriorityExecutor(String name) {
        int corePoolSize = coreSize(name, Math.max(2, Runtime.getRuntime().availableProcessors() / 2));
        int maximumPoolSize = maxSize(name, Math.max(corePoolSize, Runtime.getRuntime().availableProcessors()));

        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                60L,
                TimeUnit.SECONDS,
                new PriorityBlockingQueue<>(100),
                new NamedThreadFactory(threadName(name) + "-priority"),
                rejectionHandler(name)
        );
    }

    private ThreadingProperties.PoolProperties pool(String name) {
        if (properties == null) return null;
        return properties.getPools().getOrDefault(name, properties.getDefaultPool());
    }

    private int coreSize(String name, int fallback) {
        ThreadingProperties.PoolProperties pool = pool(name);
        return pool != null && pool.getCoreSize() > 0 ? pool.getCoreSize() : Math.max(1, fallback);
    }

    private int maxSize(String name, int fallback) {
        ThreadingProperties.PoolProperties pool = pool(name);
        int configured = pool != null && pool.getMaxSize() > 0 ? pool.getMaxSize() : fallback;
        return Math.max(coreSize(name, fallback), configured);
    }

    private long keepAlive(String name) {
        ThreadingProperties.PoolProperties pool = pool(name);
        return pool == null ? 60L : Math.max(0, pool.getKeepAliveTime());
    }

    private int queueCapacity(String name, int fallback) {
        ThreadingProperties.PoolProperties pool = pool(name);
        return pool != null && pool.getQueueCapacity() > 0 ? pool.getQueueCapacity() : fallback;
    }

    private boolean allowCoreThreadTimeOut(String name) {
        ThreadingProperties.PoolProperties pool = pool(name);
        return pool != null && pool.isAllowCoreThreadTimeOut() && keepAlive(name) > 0;
    }

    private String threadName(String name) {
        ThreadingProperties.PoolProperties pool = pool(name);
        return pool != null && pool.getThreadNamePrefix() != null && !pool.getThreadNamePrefix().isBlank()
                ? pool.getThreadNamePrefix() : name;
    }

    private java.util.concurrent.RejectedExecutionHandler rejectionHandler(String name) {
        ThreadingProperties.PoolProperties pool = pool(name);
        ThreadingProperties.RejectionPolicy policy = pool == null
                ? ThreadingProperties.RejectionPolicy.CALLER_RUNS : pool.getRejectionPolicy();
        if (policy == null) policy = ThreadingProperties.RejectionPolicy.CALLER_RUNS;
        return switch (policy) {
            case ABORT -> new ThreadPoolExecutor.AbortPolicy();
            case DISCARD -> new ThreadPoolExecutor.DiscardPolicy();
            case DISCARD_OLDEST -> new ThreadPoolExecutor.DiscardOldestPolicy();
            case CALLER_RUNS -> new ThreadPoolExecutor.CallerRunsPolicy();
        };
    }

    /**
     * 命名线程工厂
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        NamedThreadFactory(String namePrefix) {
            this.namePrefix = "shiyu-" + namePrefix + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            t.setDaemon(false);
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
