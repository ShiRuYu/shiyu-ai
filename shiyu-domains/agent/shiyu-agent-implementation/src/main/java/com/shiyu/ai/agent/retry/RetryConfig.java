package com.shiyu.ai.agent.retry;

/**
 * 重试配置
 */
public class RetryConfig {

    private final int maxRetries;
    private final long initialDelayMs;
    private final double backoffMultiplier;

    public RetryConfig(int maxRetries, long initialDelayMs, double backoffMultiplier) {
        this.maxRetries = maxRetries;
        this.initialDelayMs = initialDelayMs;
        this.backoffMultiplier = backoffMultiplier;
    }

    public static RetryConfig defaultConfig() {
        return new RetryConfig(3, 1000, 2.0);
    }

    public int getMaxRetries() { return maxRetries; }
    public long getInitialDelayMs() { return initialDelayMs; }
    public double getBackoffMultiplier() { return backoffMultiplier; }
}
