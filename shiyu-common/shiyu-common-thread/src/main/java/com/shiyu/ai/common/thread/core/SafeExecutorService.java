
package com.shiyu.ai.common.thread.core;

import com.shiyu.ai.common.thread.api.TaskContext;
import com.shiyu.ai.common.thread.api.TaskDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 安全执行器服务包装器
 * 提供上下文传递、异常处理和任务装饰等功能
 */
public class SafeExecutorService extends AbstractExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(SafeExecutorService.class);

    private final ExecutorService delegate;
    private final TaskDecorator taskDecorator;
    private final AtomicReference<State> state = new AtomicReference<>(State.RUNNING);

    /**
     * 执行器状态
     */
    private enum State {
        RUNNING, SHUTDOWN, TERMINATED
    }

    /**
     * 创建安全执行器服务
     * 
     * @param delegate 委托的执行器服务
     */
    public SafeExecutorService(ExecutorService delegate) {
        this(delegate, new ContextTaskDecorator());
    }

    /**
     * 创建安全执行器服务
     * 
     * @param delegate 委托的执行器服务
     * @param taskDecorator 任务装饰器
     */
    public SafeExecutorService(ExecutorService delegate, TaskDecorator taskDecorator) {
        this.delegate = delegate;
        this.taskDecorator = taskDecorator;
    }

    @Override
    public void execute(Runnable command) {
        if (state.get() != State.RUNNING) {
            throw new IllegalStateException("执行器已关闭，无法执行新任务");
        }

        Runnable decoratedCommand = taskDecorator.decorate(command);
        delegate.execute(decoratedCommand);
    }

    @Override
    public void shutdown() {
        if (state.compareAndSet(State.RUNNING, State.SHUTDOWN)) {
            delegate.shutdown();
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        if (state.compareAndSet(State.RUNNING, State.SHUTDOWN)) {
            return delegate.shutdownNow();
        }
        return List.of();
    }

    @Override
    public boolean isShutdown() {
        return state.get() != State.RUNNING || delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return state.get() == State.TERMINATED || delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        if (state.compareAndSet(State.SHUTDOWN, State.TERMINATED)) {
            return delegate.awaitTermination(timeout, unit);
        }
        return delegate.awaitTermination(timeout, unit);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new SafeFutureTask<>(runnable, value);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new SafeFutureTask<>(callable);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        if (state.get() != State.RUNNING) {
            throw new IllegalStateException("执行器已关闭，无法提交新任务");
        }

        Callable<T> decoratedTask = taskDecorator.decorate(task);
        return delegate.submit(decoratedTask);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        if (state.get() != State.RUNNING) {
            throw new IllegalStateException("执行器已关闭，无法提交新任务");
        }

        Runnable decoratedTask = taskDecorator.decorate(task);
        return delegate.submit(decoratedTask, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        if (state.get() != State.RUNNING) {
            throw new IllegalStateException("执行器已关闭，无法提交新任务");
        }

        Runnable decoratedTask = taskDecorator.decorate(task);
        return delegate.submit(decoratedTask);
    }

    /**
     * 上下文任务装饰器
     */
    private static class ContextTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            return new ContextAwareRunnable(runnable);
        }

        @Override
        public <V> java.util.concurrent.Callable<V> decorate(java.util.concurrent.Callable<V> callable) {
            TaskContext contextSnapshot = TaskContext.current().snapshot();
            return () -> {
                TaskContext originalContext = TaskContext.current();
                try {
                    TaskContext.current().restore(contextSnapshot);
                    return callable.call();
                } finally {
                    TaskContext.current().restore(originalContext);
                }
            };
        }
    }

    /**
     * 安全的FutureTask实现
     */
    private static class SafeFutureTask<T> extends java.util.concurrent.FutureTask<T> {

        public SafeFutureTask(Runnable runnable, T result) {
            super(new ContextAwareRunnable(runnable), result);
        }

        public SafeFutureTask(Callable<T> callable) {
            super(new ContextAwareCallable<>(callable));
        }

        @Override
        protected void done() {
            try {
                get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("任务执行被中断", e);
            } catch (Exception e) {
                logger.error("任务执行异常", e);
            }
        }
    }

    /**
     * 上下文感知的Callable
     */
    private static class ContextAwareCallable<T> implements Callable<T> {

        private final Callable<T> delegate;
        private final TaskContext contextSnapshot;

        public ContextAwareCallable(Callable<T> delegate) {
            this.delegate = delegate;
            this.contextSnapshot = TaskContext.current().snapshot();
        }

        @Override
        public T call() throws Exception {
            TaskContext originalContext = TaskContext.current();
            try {
                TaskContext.current().restore(contextSnapshot);
                return delegate.call();
            } finally {
                TaskContext.current().restore(originalContext);
            }
        }
    }
}
