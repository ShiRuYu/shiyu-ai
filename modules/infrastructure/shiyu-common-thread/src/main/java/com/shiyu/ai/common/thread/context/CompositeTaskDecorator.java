package com.shiyu.ai.common.thread.context;

import com.shiyu.ai.common.thread.api.TaskDecorator;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * 按顺序组合多个任务装饰器并传递执行上下文。
 */
public class CompositeTaskDecorator implements TaskDecorator {

    /**
     * decorators 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final List<TaskDecorator> decorators;

    /**
     * {@code CompositeTaskDecorator} 创建并初始化当前类型实例。
     *
     * @param decorators 参数值，用于执行当前操作。
     */
    public CompositeTaskDecorator(List<TaskDecorator> decorators) {
        this.decorators = List.copyOf(decorators);
    }

    /**
     * {@code decorate} 执行当前类型定义的业务操作。
     *
     * @param runnable 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Runnable decorate(Runnable runnable) {
        Runnable decorated = runnable;
        for (int index = decorators.size() - 1; index >= 0; index--) {
            decorated = decorators.get(index).decorate(decorated);
        }
        return decorated;
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
        Callable<V> decorated = callable;
        for (int index = decorators.size() - 1; index >= 0; index--) {
            decorated = decorators.get(index).decorate(decorated);
        }
        return decorated;
    }
}
