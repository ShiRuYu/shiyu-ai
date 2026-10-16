package com.shiyu.ai.governance.implementation.usage.realtime;

/**
 * 发布 用量 Realtime 相关的领域事件或基础设施消息。
 */
public interface UsageRealtimePublisher {
    /**
     * 推送一次 LLM 调用用量；模型价格未配置时，费用为 {@code null}。
     *
     * @param platform 模型服务商标识。
     * @param model 模型标识。
     * @param promptTokens 输入 Token 数。
     * @param completionTokens 输出 Token 数。
     * @param latencyMs 调用延迟。
     * @param cost USD 费用；价格未知时为 {@code null}。
     */
    void publishUsageRecord(
            String platform,
            String model,
            int promptTokens,
            int completionTokens,
            long latencyMs,
            Double cost);

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
