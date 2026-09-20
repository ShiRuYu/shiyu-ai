package com.shiyu.ai.agent.implementation.retry;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * 实现 Retry Policy Impl 相关的业务处理、协作逻辑或基础设施能力。
 */
@Slf4j
public class RetryPolicyImpl implements RetryPolicy {

    /**
     * 调用 Retry Policy Impl 相关业务数据，并返回处理结果。
     *
     * @param supplier 用于完成本次业务处理的 supplier 参数。
     * @param config 用于完成本次业务处理的 config 参数。
     * @return 返回 Retry Policy Impl 相关操作生成的结果数据。
     */
    @Override
    public <T> T executeWithRetry(Supplier<T> supplier, RetryConfig config) {
        int maxRetries = config.getMaxRetries();
        long delayMs = config.getInitialDelayMs();
        double multiplier = config.getBackoffMultiplier();

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return supplier.get();
            } catch (Exception e) {
                if (attempt >= maxRetries) {
                    log.error("重试耗尽 ({}次)，最终失败", maxRetries, e);
                    throw e;
                }
                log.warn("第 {} 次重试失败，{}ms 后重试", attempt + 1, delayMs);
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
                delayMs = (long) (delayMs * multiplier);
            }
        }
        throw new RuntimeException("重试执行异常");
    }
}
