package com.shiyu.ai.common.thread.context;

import com.shiyu.ai.common.thread.api.TaskDecorator;

import java.util.concurrent.Callable;

/**
 * 实现 Context Task Decorator 相关的业务处理、协作逻辑或基础设施能力。
 */
public class ContextTaskDecorator implements TaskDecorator {

    /**
     * 执行 Context Task Decorator 相关业务数据，并返回处理结果。
     *
     * @param runnable 用于完成本次业务处理的 runnable 参数。
     * @return 返回 Context Task Decorator 相关操作生成的结果数据。
     */
    @Override
    public Runnable decorate(Runnable runnable) {
        return new ContextAwareRunnable(runnable);
    }

    /**
     * 执行 Context Task Decorator 相关业务数据，并返回处理结果。
     *
     * @param callable 用于完成本次业务处理的 callable 参数。
     * @return 返回 Context Task Decorator 相关操作生成的结果数据。
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
