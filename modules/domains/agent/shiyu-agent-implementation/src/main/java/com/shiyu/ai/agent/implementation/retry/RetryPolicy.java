package com.shiyu.ai.agent.implementation.retry;

import java.util.function.Supplier;

/** 重试策略 */
public interface RetryPolicy {

    /**
     * 执行当前接口定义的业务流程。
     *
     * @param supplier 方法参数。
     * @param config 配置参数。
     *
     * @return 操作结果。
     */
    <T> T executeWithRetry(Supplier<T> supplier, RetryConfig config);
}
