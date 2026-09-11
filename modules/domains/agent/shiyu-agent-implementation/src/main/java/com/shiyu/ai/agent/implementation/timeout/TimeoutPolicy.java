package com.shiyu.ai.agent.implementation.timeout;

import java.util.concurrent.Callable;

/** 超时策略 */
public interface TimeoutPolicy extends AutoCloseable {

    <T> T executeWithTimeout(Callable<T> callable, TimeoutConfig config) throws Exception;

    @Override
    default void close() {
        // Policies without an executor do not need shutdown work.
    }
}
