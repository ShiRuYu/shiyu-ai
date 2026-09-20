package com.shiyu.ai.common.thread.context;

import java.util.concurrent.Callable;

/**
 * 实现 Context Aware Callable 相关的业务处理、协作逻辑或基础设施能力。
 */
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
     * 执行 Context Aware Callable 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param delegate 用于完成本次业务处理的 delegate 参数。
     */
    public ContextAwareCallable(Callable<T> delegate) {
        this.delegate = delegate;
        this.contextSnapshot = TaskContext.current().snapshot();
    }

    /**
     * 调用 Context Aware Callable 相关业务数据，并返回处理结果。
     *
     * @return 返回 Context Aware Callable 相关操作生成的结果数据。
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
