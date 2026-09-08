package com.shiyu.ai.agent.timeout;

/**
 * 超时配置
 */
public class TimeoutConfig {

    private final long globalTimeoutMs;
    private final long nodeTimeoutMs;

    public TimeoutConfig(long globalTimeoutMs, long nodeTimeoutMs) {
        this.globalTimeoutMs = globalTimeoutMs;
        this.nodeTimeoutMs = nodeTimeoutMs;
    }

    public static TimeoutConfig defaultConfig() {
        return new TimeoutConfig(300000, 60000); // 全局5分钟，节点1分钟
    }

    public long getGlobalTimeoutMs() { return globalTimeoutMs; }
    public long getNodeTimeoutMs() { return nodeTimeoutMs; }
}
