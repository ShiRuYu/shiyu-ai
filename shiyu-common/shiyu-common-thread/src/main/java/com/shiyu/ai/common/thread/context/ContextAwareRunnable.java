package com.shiyu.ai.common.thread.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * 上下文感知的Runnable实现
 * 能够在任务执行时保持线程上下文的一致性
 */
public class ContextAwareRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ContextAwareRunnable.class);

    private final Runnable delegate;
    private final TaskContext contextSnapshot;

    /**
     * 创建上下文感知的Runnable
     *
     * @param delegate 委托的Runnable任务
     */
    public ContextAwareRunnable(Runnable delegate) {
        this.delegate = delegate;
        this.contextSnapshot = TaskContext.current().snapshot();
    }

    @Override
    public void run() {
        TaskContext originalContext = TaskContext.current();
        try {
            TaskContext.current().restore(contextSnapshot);
            delegate.run();
        } catch (Exception e) {
            logger.error("任务执行异常", e);
            throw e;
        } finally {
            TaskContext.current().restore(originalContext);
        }
    }

    /**
     * 异步执行任务
     *
     * @param runnable 任务
     * @return CompletableFuture
     */
    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(new ContextAwareRunnable(runnable));
    }

    /**
     * 异步执行有返回值的任务
     *
     * @param supplier 任务提供者
     * @param <T> 返回值类型
     * @return CompletableFuture
     */
    public static <T> CompletableFuture<T> supplyAsync(java.util.function.Supplier<T> supplier) {
        TaskContext contextSnapshot = TaskContext.current().snapshot();
        return CompletableFuture.supplyAsync(() -> {
            TaskContext originalContext = TaskContext.current();
            try {
                TaskContext.current().restore(contextSnapshot);
                return supplier.get();
            } finally {
                TaskContext.current().restore(originalContext);
            }
        });
    }
}