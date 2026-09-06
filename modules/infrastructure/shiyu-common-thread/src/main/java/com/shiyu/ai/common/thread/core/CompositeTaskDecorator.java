package com.shiyu.ai.common.thread.core;

import com.shiyu.ai.common.thread.api.TaskDecorator;

import java.util.List;
import java.util.concurrent.Callable;

/** Applies task decorators in declaration order. */
public class CompositeTaskDecorator implements TaskDecorator {

    private final List<TaskDecorator> decorators;

    public CompositeTaskDecorator(List<TaskDecorator> decorators) {
        this.decorators = List.copyOf(decorators);
    }

    @Override
    public Runnable decorate(Runnable runnable) {
        Runnable decorated = runnable;
        for (int index = decorators.size() - 1; index >= 0; index--) {
            decorated = decorators.get(index).decorate(decorated);
        }
        return decorated;
    }

    @Override
    public <V> Callable<V> decorate(Callable<V> callable) {
        Callable<V> decorated = callable;
        for (int index = decorators.size() - 1; index >= 0; index--) {
            decorated = decorators.get(index).decorate(decorated);
        }
        return decorated;
    }
}
