package com.shiyu.ai.agent.implementation.timeout;

import java.util.concurrent.Callable;

/** 超时策略 */
public interface TimeoutPolicy extends AutoCloseable {

    /**
     * 执行当前接口定义的业务流程。
     *
     * @param callable 方法参数。
     * @param config 配置参数。
     *
     * @return 操作结果。
     */
    <T> T executeWithTimeout(Callable<T> callable, TimeoutConfig config) throws Exception;

    /**
     * 执行 {@code close} 定义的接口操作。
     */
    @Override
    default void close() {
    }
}
