package com.shiyu.ai.governance.implementation.usage.realtime;

/** Optional transport adapter for usage notifications. */
public interface UsageRealtimePublisher {
    void publishUsageRecord(String platform, String model, int promptTokens,
                            int completionTokens, long latencyMs, double cost);

    void publishEmbeddingUsage(String model, int textLength, int estimatedTokens,
                               int vectorCount, long latencyMs);
}
