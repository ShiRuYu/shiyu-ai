package com.shiyu.ai.common.thread.executor;

import com.shiyu.ai.common.thread.api.TaskDecorator;
import com.shiyu.ai.common.thread.context.ContextAwareCallable;
import com.shiyu.ai.common.thread.context.ContextAwareRunnable;
import com.shiyu.ai.common.thread.context.ContextTaskDecorator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 提供 Safe Executor 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public class SafeExecutorService extends AbstractExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(SafeExecutorService.class);

    /**
     * delegate 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ExecutorService delegate;
    /**
     * taskDecorator 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final TaskDecorator taskDecorator;
    private final AtomicReference<State> state = new AtomicReference<>(State.RUNNING);

    /**
     * 定义 State 可用的枚举值及其业务语义。
     */
    private enum State {
        RUNNING,
        SHUTDOWN,
        TERMINATED
    }

    /**
     * 获取委托的执行器服务
     *
     * @return 委托的执行器服务
     */
    public ExecutorService getDelegate() {
        return delegate;
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

    /**
     * 调用 Safe Executor 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param command 本次流程携带的事件或业务数据。
     */
    @Override
    public void execute(Runnable command) {
        if (state.get() != State.RUNNING) {
            throw new IllegalStateException("执行器已关闭，无法执行新任务");
        }

        Runnable decoratedCommand = taskDecorator.decorate(command);
        delegate.execute(decoratedCommand);
    }

    /**
     * 执行 Safe Executor 相关业务操作，并维护必要的状态和协作关系。
     */
    @Override
    public void shutdown() {
        if (state.compareAndSet(State.RUNNING, State.SHUTDOWN)) {
            delegate.shutdown();
        }
    }

    /**
     * 执行 Safe Executor 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<Runnable> shutdownNow() {
        if (state.get() == State.TERMINATED) return List.of();
        state.set(State.SHUTDOWN);
        return delegate.shutdownNow();
    }

    /**
     * 校验或判断 Safe Executor 相关业务数据，并返回处理结果。
     *
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean isShutdown() {
        return state.get() != State.RUNNING || delegate.isShutdown();
    }

    /**
     * 校验或判断 Safe Executor 相关业务数据，并返回处理结果。
     *
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean isTerminated() {
        return state.get() == State.TERMINATED || delegate.isTerminated();
    }

    /**
     * 执行 Safe Executor 相关业务数据，并返回处理结果。
     *
     * @param timeout 用于完成本次业务处理的 timeout 参数。
     * @param unit 用于完成本次业务处理的 unit 参数。
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        boolean terminated = delegate.awaitTermination(timeout, unit);
        if (terminated) state.set(State.TERMINATED);
        return terminated;
    }

    /**
     * 执行 Safe Executor 相关业务数据，并返回处理结果。
     *
     * @param runnable 用于完成本次业务处理的 runnable 参数。
     * @param value 用于完成本次业务处理的 value 参数。
     * @return 返回 Safe Executor 相关操作生成的结果数据。
     */
    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new SafeFutureTask<>(runnable, value);
    }

    /**
     * 执行 Safe Executor 相关业务数据，并返回处理结果。
     *
     * @param callable 用于完成本次业务处理的 callable 参数。
     * @return 返回 Safe Executor 相关操作生成的结果数据。
     */
    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new SafeFutureTask<>(callable);
    }

    /**
     * 执行 Safe Executor 相关业务数据，并返回处理结果。
     *
     * @param task 用于完成本次业务处理的 task 参数。
     * @return 返回 Safe Executor 相关操作生成的结果数据。
     */
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        if (state.get() != State.RUNNING) {
            throw new IllegalStateException("执行器已关闭，无法提交新任务");
        }

        Callable<T> decoratedTask = taskDecorator.decorate(task);
        return delegate.submit(decoratedTask);
    }

    /**
     * 执行 Safe Executor 相关业务数据，并返回处理结果。
     *
     * @param task 用于完成本次业务处理的 task 参数。
     * @param result 用于完成本次业务处理的 result 参数。
     * @return 返回 Safe Executor 相关操作生成的结果数据。
     */
    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        if (state.get() != State.RUNNING) {
            throw new IllegalStateException("执行器已关闭，无法提交新任务");
        }

        Runnable decoratedTask = taskDecorator.decorate(task);
        return delegate.submit(decoratedTask, result);
    }

    /**
     * 执行 Safe Executor 相关业务数据，并返回处理结果。
     *
     * @param task 用于完成本次业务处理的 task 参数。
     * @return 返回 Safe Executor 相关操作生成的结果数据。
     */
    @Override
    public Future<?> submit(Runnable task) {
        if (state.get() != State.RUNNING) {
            throw new IllegalStateException("执行器已关闭，无法提交新任务");
        }

        Runnable decoratedTask = taskDecorator.decorate(task);
        return delegate.submit(decoratedTask);
    }

    /**
     * 实现 Safe Future 相关的业务处理、协作逻辑或基础设施能力。
     */
    private static class SafeFutureTask<T> extends FutureTask<T> {

        /**
         * 执行 Safe Future 相关业务操作，并维护必要的状态和协作关系。
         *
         * @param runnable 用于完成本次业务处理的 runnable 参数。
         * @param result 用于完成本次业务处理的 result 参数。
         */
        public SafeFutureTask(Runnable runnable, T result) {
            super(new ContextAwareRunnable(runnable), result);
        }

        /**
         * 执行 Safe Future 相关业务操作，并维护必要的状态和协作关系。
         *
         * @param callable 用于完成本次业务处理的 callable 参数。
         */
        public SafeFutureTask(Callable<T> callable) {
            super(new ContextAwareCallable<>(callable));
        }

        /**
         * 执行 Safe Future 相关业务操作，并维护必要的状态和协作关系。
         */
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
}
