package com.shiyu.ai.agent.implementation.retry;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/** 默认重试策略实现（指数退避） */
@Slf4j
public class RetryPolicyImpl implements RetryPolicy {

    /**
     * {@code executeWithRetry} 执行当前模块定义的业务流程。
     *
     * @param supplier 参数值，用于执行当前操作。
     * @param config 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
