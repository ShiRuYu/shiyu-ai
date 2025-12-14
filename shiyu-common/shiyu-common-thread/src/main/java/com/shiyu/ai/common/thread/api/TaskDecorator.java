
package com.shiyu.ai.common.thread.api;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * 任务装饰器接口
 * 用于在线程池执行任务前后添加额外的处理逻辑，如上下文传递、监控等
 */
@FunctionalInterface
public interface TaskDecorator {

    /**
     * 装饰Runnable任务
     * 
     * @param runnable 原始任务
     * @return 装饰后的任务
     */
    Runnable decorate(Runnable runnable);

    /**
     * 装饰Callable任务
     * 
     * @param callable 原始任务
     * @return 装饰后的任务
     */
    default <V> Callable<V> decorate(Callable<V> callable) {
        return () -> {
            try {
                decorate(() -> {}).run();
                return callable.call();
            } finally {
                // 清理资源
            }
        };
    }

    /**
     * 装饰Supplier任务
     * 
     * @param supplier 原始任务
     * @return 装饰后的任务
     */
    default <T> Supplier<T> decorate(Supplier<T> supplier) {
        return () -> {
            try {
                decorate(() -> {}).run();
                return supplier.get();
            } finally {
                // 清理资源
            }
        };
    }
}
