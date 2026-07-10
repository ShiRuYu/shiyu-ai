package com.shiyu.ai.agent.retry;

import java.util.function.Supplier;

/**
 * 重试策略
 */
public interface RetryPolicy {

    <T> T executeWithRetry(Supplier<T> supplier, RetryConfig config);
}
