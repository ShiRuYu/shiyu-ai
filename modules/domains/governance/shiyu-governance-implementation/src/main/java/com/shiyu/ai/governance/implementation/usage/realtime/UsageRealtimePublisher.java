package com.shiyu.ai.governance.implementation.usage.realtime;

/**
 * UsageRealtimePublisher 接口，定义治理模块的能力边界。
 */
public interface UsageRealtimePublisher {
    /**
     * 发布或发送业务事件。
     *
     * @param platform 方法参数。
     * @param model 方法参数。
     * @param promptTokens 方法参数。
     * @param completionTokens 方法参数。
     * @param latencyMs 方法参数。
     * @param cost 方法参数。
     */
    void publishUsageRecord(
            String platform,
            String model,
            int promptTokens,
            int completionTokens,
            long latencyMs,
            double cost);

    /**
     * 发布或发送业务事件。
     *
     * @param model 方法参数。
     * @param textLength 方法参数。
     * @param estimatedTokens 方法参数。
     * @param vectorCount 方法参数。
     * @param latencyMs 方法参数。
     */
    void publishEmbeddingUsage(
            String model, int textLength, int estimatedTokens, int vectorCount, long latencyMs);
}
