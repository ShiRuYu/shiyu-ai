package com.shiyu.ai.common.thread.context;

import com.shiyu.ai.common.thread.api.TaskDecorator;

import java.util.concurrent.Callable;

/**
 * 在线程切换时传播租户、用户和追踪上下文。
 */
public class ContextTaskDecorator implements TaskDecorator {

    /**
     * {@code decorate} 执行当前类型定义的业务操作。
     *
     * @param runnable 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Runnable decorate(Runnable runnable) {
        return new ContextAwareRunnable(runnable);
    }

    /**
     * {@code decorate} 执行当前类型定义的业务操作。
     *
     * @param callable 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public <V> Callable<V> decorate(Callable<V> callable) {
        TaskContext snapshot = TaskContext.current().snapshot();
        return () -> {
            TaskContext original = TaskContext.current().snapshot();
            try {
                TaskContext.current().restore(snapshot);
                return callable.call();
            } finally {
                TaskContext.current().restore(original);
            }
        };
    }
}
