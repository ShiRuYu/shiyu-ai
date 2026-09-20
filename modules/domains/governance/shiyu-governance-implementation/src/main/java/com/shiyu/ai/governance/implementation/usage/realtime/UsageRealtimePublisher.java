package com.shiyu.ai.governance.implementation.usage.realtime;

/**
 * 发布 用量 Realtime 相关的领域事件或基础设施消息。
 */
public interface UsageRealtimePublisher {
    /**
     * 发布或发送 用量 Realtime 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param platform 用于完成本次业务处理的 platform 参数。
     * @param model 用于完成本次业务处理的 model 参数。
     * @param promptTokens 用于完成本次业务处理的 promptTokens 参数。
     * @param completionTokens 用于完成本次业务处理的 completionTokens 参数。
     * @param latencyMs 用于完成本次业务处理的 latencyMs 参数。
     * @param cost 用于完成本次业务处理的 cost 参数。
     */
    void publishUsageRecord(
            String platform,
            String model,
            int promptTokens,
            int completionTokens,
            long latencyMs,
            double cost);

    /**
     * 发布或发送 用量 Realtime 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param model 用于完成本次业务处理的 model 参数。
     * @param textLength 用于完成本次业务处理的 textLength 参数。
     * @param estimatedTokens 用于完成本次业务处理的 estimatedTokens 参数。
     * @param vectorCount 用于完成本次业务处理的 vectorCount 参数。
     * @param latencyMs 用于完成本次业务处理的 latencyMs 参数。
     */
    void publishEmbeddingUsage(
            String model, int textLength, int estimatedTokens, int vectorCount, long latencyMs);
}
