package com.shiyu.ai.agent.implementation.retry;

import java.util.function.Supplier;

/**
 * 校验或约束 Retry 相关的请求、状态和访问规则。
 */
public interface RetryPolicy {

    /**
     * 调用 Retry 相关业务数据，并返回处理结果。
     *
     * @param supplier 用于完成本次业务处理的 supplier 参数。
     * @param config 用于完成本次业务处理的 config 参数。
     * @return 返回 Retry 相关操作生成的结果数据。
     */
    <T> T executeWithRetry(Supplier<T> supplier, RetryConfig config);
}
