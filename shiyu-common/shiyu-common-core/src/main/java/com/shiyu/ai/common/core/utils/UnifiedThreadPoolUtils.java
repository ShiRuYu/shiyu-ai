package com.shiyu.ai.common.core.utils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unified Thread Pool Utils for JDK8 ~ JDK21
 *
 * - JDK21+: Use Virtual Threads (Executors.newVirtualThreadPerTaskExecutor)
 * - JDK8~20: Use ThreadPoolExecutor
 * - Support multiple named pools
 */
public class UnifiedThreadPoolUtils {

    /** Default global executor */
    private static final ExecutorService DEFAULT_EXECUTOR;

    /** Named thread pools cache */
    private static final Map<String, ExecutorService> NAMED_EXECUTORS = new ConcurrentHashMap<>();

    /** Default ThreadPool config (for JDK8) */
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;
    private static final int QUEUE_CAPACITY = 1000;
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

    public static void execute(Runnable task) {
        try {
            DEFAULT_EXECUTOR.execute(wrapRunnable(task));
        } catch (Exception e) {
            System.err.println("[ThreadPool] Failed to execute task: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static <T> Future<T> submit(Callable<T> task) {
        try {
            return DEFAULT_EXECUTOR.submit(wrapCallable(task));
        } catch (Exception e) {
            System.err.println("[ThreadPool] Failed to submit task: " + e.getMessage());
            e.printStackTrace();
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 根据线程池名称关闭线程池
     */
    public static void shutdown(String poolName) {
        ExecutorService executor = NAMED_EXECUTORS.remove(poolName);
        if (executor != null) {
            try {
                System.out.println("[ThreadPool] Shutting down pool: " + poolName);
                executor.shutdown();
            } catch (Exception e) {
                System.err.println("[ThreadPool] Failed to shutdown pool: " + poolName + " , " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("[ThreadPool] No pool found with name: " + poolName);
        }
    }

    /**
     * 根据ExecutorService关闭线程池
     */
    public static void shutdown(ExecutorService executor) {
        if (executor == null) {
            return;
        }
        try {
            // 查找是否存在于 NAMED_EXECUTORS
            NAMED_EXECUTORS.entrySet().removeIf(entry -> entry.getValue() == executor);
            if (executor == DEFAULT_EXECUTOR) {
                System.out.println("[ThreadPool] Shutting down DEFAULT executor");
            }
            executor.shutdown();
        } catch (Exception e) {
            System.err.println("[ThreadPool] Failed to shutdown executor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        try {
            for (Map.Entry<String, ExecutorService> entry : NAMED_EXECUTORS.entrySet()) {
                System.out.println("[ThreadPool] Shutting down pool: " + entry.getKey());
                entry.getValue().shutdown();
            }
        } catch (Exception e) {
            System.err.println("[ThreadPool] Shutdown error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static ExecutorService getExecutor() {
        return DEFAULT_EXECUTOR;
    }

    /**
     * Get or create a named thread pool JDK8 only
     */
    public static ExecutorService getNamedExecutor(String name) {
        return NAMED_EXECUTORS.computeIfAbsent(name, UnifiedThreadPoolUtils::createThreadPoolExecutor);
    }

    // ========================= Internal =========================

    /** Wrap runnable with exception handler */
    private static Runnable wrapRunnable(Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Throwable t) {
                System.err.println("[ThreadPool] Task error: " + t.getMessage());
                t.printStackTrace();
            }
        };
    }

    /** Wrap callable with exception handler */
    private static <T> Callable<T> wrapCallable(Callable<T> task) {
        return () -> {
            try {
                return task.call();
            } catch (Throwable t) {
                System.err.println("[ThreadPool] Task error: " + t.getMessage());
                t.printStackTrace();
                throw t;
            }
        };
    }

    /** Check whether Virtual Threads are supported (JDK21+) */
    private static boolean isVirtualThreadSupported() {
        try {
            Method m = Executors.class.getMethod("newVirtualThreadPerTaskExecutor");
            return m != null;
        } catch (Throwable e) {
            return false;
        }
    }

    /** Create Virtual Thread Executor (JDK21+) */
    private static ExecutorService createVirtualThreadExecutor() {
        try {
            Method m = Executors.class.getMethod("newVirtualThreadPerTaskExecutor");
            return (ExecutorService) m.invoke(null);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create VirtualThread Executor", e);
        }
    }

    /** Create JDK8 ThreadPoolExecutor with name prefix, and put into NAMED_EXECUTORS */
    private static ExecutorService createThreadPoolExecutor(String poolName) {
        ThreadFactory factory = new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(1);

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
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * Sleep 等待（毫秒）
     * 建议在任务中使用，不会抛出受检异常
     */
    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            // 恢复中断状态，保证上层代码能感知
            Thread.currentThread().interrupt();
            System.err.println("[ThreadPool] Sleep interrupted: " + e.getMessage());
        }
    }

    /**
     * Sleep 等待（秒）
     */
    public static void sleepSeconds(long seconds) {
        sleep(TimeUnit.SECONDS.toMillis(seconds));
    }

    /**
     * 打印线程异常信息
     * 用于线程池执行任务时捕获 Future 异常
     */
    public static void printException(Runnable r, Throwable t) {
        if (t == null && r instanceof Future<?>) {
            try {
                Future<?> future = (Future<?>) r;
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
            System.err.println("[ThreadPool] Uncaught exception: " + t.getMessage());
            t.printStackTrace(System.err);
        }
    }

}
