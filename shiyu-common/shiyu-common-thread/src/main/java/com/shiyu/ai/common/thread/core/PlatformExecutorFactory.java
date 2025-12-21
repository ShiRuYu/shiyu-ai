
package com.shiyu.ai.common.thread.core;

import com.shiyu.ai.common.thread.api.PoolType;

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
        int corePoolSize = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        int maximumPoolSize = Runtime.getRuntime().availableProcessors();
        long keepAliveTime = 60L;

        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new NamedThreadFactory(name),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 创建CPU密集型任务线程池
     * 
     * @param name 线程池名称
     * @return 线程池执行器
     */
    private ExecutorService createCpuIntensiveExecutor(String name) {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        int maximumPoolSize = corePoolSize;

        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(50),
                new NamedThreadFactory(name + "-cpu"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 创建IO密集型任务线程池
     * 
     * @param name 线程池名称
     * @return 线程池执行器
     */
    private ExecutorService createIoIntensiveExecutor(String name) {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        int maximumPoolSize = corePoolSize * 2;
        long keepAliveTime = 60L;

        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                new NamedThreadFactory(name + "-io"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 创建调度任务线程池
     * 
     * @param name 线程池名称
     * @return 调度线程池执行器
     */
    private ScheduledThreadPoolExecutor createScheduledExecutor(String name) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
                Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
                new NamedThreadFactory(name + "-scheduled")
        );
        // 设置移除已取消任务的策略
        executor.setRemoveOnCancelPolicy(true);
        return executor;
    }

    /**
     * 创建优先级任务线程池
     * 
     * @param name 线程池名称
     * @return 线程池执行器
     */
    private ExecutorService createPriorityExecutor(String name) {
        int corePoolSize = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        int maximumPoolSize = Runtime.getRuntime().availableProcessors();

        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                60L,
                TimeUnit.SECONDS,
                new PriorityBlockingQueue<>(100),
                new NamedThreadFactory(name + "-priority"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
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
