package com.shiyu.ai.common.thread.context;

import java.util.concurrent.Callable;

/** 上下文感知的Callable */
public class ContextAwareCallable<T> implements Callable<T> {

    /**
     * delegate 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final Callable<T> delegate;
    /**
     * contextSnapshot 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final TaskContext contextSnapshot;

    /**
     * {@code ContextAwareCallable} 创建并初始化当前类型实例。
     *
     * @param delegate 参数值，用于执行当前操作。
     */
    public ContextAwareCallable(Callable<T> delegate) {
        this.delegate = delegate;
        this.contextSnapshot = TaskContext.current().snapshot();
    }

    /**
     * {@code call} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
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
