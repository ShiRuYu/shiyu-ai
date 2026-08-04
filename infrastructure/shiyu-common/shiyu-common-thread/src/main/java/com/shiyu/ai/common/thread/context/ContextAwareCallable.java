package com.shiyu.ai.common.thread.context;

import java.util.concurrent.Callable;

/**
 * 上下文感知的Callable
 */
public class ContextAwareCallable<T> implements Callable<T> {

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