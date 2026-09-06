package com.shiyu.ai.common.thread.context;

import com.shiyu.ai.common.thread.api.TaskDecorator;

import java.util.concurrent.Callable;

/** Propagates the common task context to managed worker threads. */
public class ContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        return new ContextAwareRunnable(runnable);
    }

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
