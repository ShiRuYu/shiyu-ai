package com.shiyu.ai.common.core.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 提供统一的线程池创建、命名和关闭能力。
 */
@Slf4j
public class UnifiedThreadPoolUtils {

    /** DEFAULT_EXECUTOR 字段，保存默认线程池执行器。 */
    private static final ExecutorService DEFAULT_EXECUTOR;

    /** 按名称缓存的线程池执行器。 */
    private static final Map<String, ExecutorService> NAMED_EXECUTORS = new ConcurrentHashMap<>();

    /** 线程池核心线程数。 */
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    /** 线程池最大线程数。 */
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;

    /** 线程池任务队列容量。 */
    private static final int QUEUE_CAPACITY = 1000;

    /** 线程空闲保活时间（秒）。 */
    private static final long KEEP_ALIVE_TIME = 60L;

    static {
        ExecutorService tmp;
        if (isVirtualThreadSupported()) {
            tmp = createVirtualThreadExecutor();
        } else {
            tmp = createThreadPoolExecutor("default");
        }
        DEFAULT_EXECUTOR = tmp;
        NAMED_EXECUTORS.put("default", DEFAULT_EXECUTOR); // 默认池也缓存起来
    }

    // ========================= API =========================

    /**
     * {@code execute} 执行当前模块定义的业务流程。
     *
     * @param task 参数值，用于执行当前操作。
     */
    public static void execute(Runnable task) {
        try {
            DEFAULT_EXECUTOR.execute(wrapRunnable(task));
        } catch (Exception e) {
            log.error("[ThreadPool] Failed to execute task", e);
        }
    }

    /**
     * {@code submit} 执行当前类型定义的业务操作。
     *
     * @param task 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static <T> Future<T> submit(Callable<T> task) {
        try {
            return DEFAULT_EXECUTOR.submit(wrapCallable(task));
        } catch (Exception e) {
            log.error("[ThreadPool] Failed to submit task", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /** 根据线程池名称关闭线程池 */
    public static void shutdown(String poolName) {
        ExecutorService executor = NAMED_EXECUTORS.remove(poolName);
        if (executor != null) {
            try {
                log.info("[ThreadPool] Shutting down pool: {}", poolName);
                executor.shutdown();
            } catch (Exception e) {
                log.error("[ThreadPool] Failed to shutdown pool: {}", poolName, e);
            }
        } else {
            log.info("[ThreadPool] No pool found with name: {}", poolName);
        }
    }

    /** 根据ExecutorService关闭线程池 */
    public static void shutdown(ExecutorService executor) {
        if (executor == null) {
            return;
        }
        try {
            // 查找是否存在于 NAMED_EXECUTORS
            NAMED_EXECUTORS.entrySet().removeIf(entry -> entry.getValue() == executor);
            if (executor == DEFAULT_EXECUTOR) {
                log.info("[ThreadPool] Shutting down DEFAULT executor");
            }
            executor.shutdown();
        } catch (Exception e) {
            log.error("[ThreadPool] Failed to shutdown executor", e);
        }
    }

    /**
     * {@code shutdown} 执行当前类型定义的业务操作。
     */
    public static void shutdown() {
        try {
            for (Map.Entry<String, ExecutorService> entry : NAMED_EXECUTORS.entrySet()) {
                log.info("[ThreadPool] Shutting down pool: {}", entry.getKey());
                entry.getValue().shutdown();
            }
        } catch (Exception e) {
            log.error("[ThreadPool] Shutdown error", e);
        }
    }

    /**
     * {@code getExecutor} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public static ExecutorService getExecutor() {
        return DEFAULT_EXECUTOR;
    }

    /**
     * 获取命名执行器。
     *
     * @param name 名称。
     *
     * @return 处理结果。
     */
    public static ExecutorService getNamedExecutor(String name) {
        return NAMED_EXECUTORS.computeIfAbsent(
                name, UnifiedThreadPoolUtils::createThreadPoolExecutor);
    }

    // ========================= Internal =========================

    /**
     * 封装Runnable 任务。
     *
     * @param task 待执行任务。
     *
     * @return 处理结果。
     */
    private static Runnable wrapRunnable(Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Throwable t) {
                log.error("[ThreadPool] Task error", t);
            }
        };
    }

    /**
     * 封装Callable 任务。
     *
     * @param task 待执行任务。
     *
     * @return 处理结果。
     */
    private static <T> Callable<T> wrapCallable(Callable<T> task) {
        return () -> {
            try {
                return task.call();
            } catch (Throwable t) {
                log.error("[ThreadPool] Task error", t);
                throw t;
            }
        };
    }

    /**
     * 判断virtual线程supported是否满足条件。
     *
     * @return 判断结果。
     */
    private static boolean isVirtualThreadSupported() {
        try {
            Method m = Executors.class.getMethod("newVirtualThreadPerTaskExecutor");
            return m != null;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * 创建virtual线程执行器。
     *
     * @return 处理结果。
     */
    private static ExecutorService createVirtualThreadExecutor() {
        try {
            Method m = Executors.class.getMethod("newVirtualThreadPerTaskExecutor");
            return (ExecutorService) m.invoke(null);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create VirtualThread Executor", e);
        }
    }

    /**
     * 创建线程线程池执行器。
     *
     * @param poolName 线程池名称。
     *
     * @return 处理结果。
     */
    private static ExecutorService createThreadPoolExecutor(String poolName) {
        ThreadFactory factory =
                new ThreadFactory() {
                    private final AtomicInteger count = new AtomicInteger(1);

                    /**
                     * {@code newThread} 执行当前类型定义的业务操作。
                     *
                     * @param r 参数值，用于执行当前操作。
                     *
                     * @return 返回当前操作产生的结果。
                     */
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, poolName + "-pool-" + count.getAndIncrement());
                    }
                };

        return new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                factory,
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /** Sleep 等待（毫秒） 建议在任务中使用，不会抛出受检异常 */
    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            // 恢复中断状态，保证上层代码能感知
            Thread.currentThread().interrupt();
            log.error(
                    "[ThreadPool] Sleep interrupted: errorType={}, errorMessageLength={}",
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
        }
    }

    /** Sleep 等待（秒） */
    public static void sleepSeconds(long seconds) {
        sleep(TimeUnit.SECONDS.toMillis(seconds));
    }

    /** 打印线程异常信息 用于线程池执行任务时捕获 Future 异常 */
    public static void printException(Runnable r, Throwable t) {
        if (t == null && r instanceof Future<?> future) {
            try {
                if (future.isDone()) {
                    future.get(); // 主动触发可能的 ExecutionException
                }
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                t = ie;
            }
        }
        if (t != null) {
            log.error("[ThreadPool] Uncaught exception: {}", t.getMessage());
        }
    }
}
