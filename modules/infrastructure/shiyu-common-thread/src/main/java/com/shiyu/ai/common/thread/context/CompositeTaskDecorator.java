package com.shiyu.ai.common.thread.context;

import com.shiyu.ai.common.thread.api.TaskDecorator;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * 实现 Composite Task Decorator 相关的业务处理、协作逻辑或基础设施能力。
 */
public class CompositeTaskDecorator implements TaskDecorator {

    /**
     * decorators 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final List<TaskDecorator> decorators;

    /**
     * 执行 Composite Task Decorator 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param decorators 用于完成本次业务处理的 decorators 参数。
     */
    public CompositeTaskDecorator(List<TaskDecorator> decorators) {
        this.decorators = List.copyOf(decorators);
    }

    /**
     * 执行 Composite Task Decorator 相关业务数据，并返回处理结果。
     *
     * @param runnable 用于完成本次业务处理的 runnable 参数。
     * @return 返回 Composite Task Decorator 相关操作生成的结果数据。
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
     * 执行 Composite Task Decorator 相关业务数据，并返回处理结果。
     *
     * @param callable 用于完成本次业务处理的 callable 参数。
     * @return 返回 Composite Task Decorator 相关操作生成的结果数据。
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
