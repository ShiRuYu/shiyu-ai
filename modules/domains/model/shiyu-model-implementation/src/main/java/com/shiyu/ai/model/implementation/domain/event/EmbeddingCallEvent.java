package com.shiyu.ai.model.implementation.domain.event;

import com.shiyu.ai.kernel.context.CorrelationId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import java.util.UUID;

/**
 * 嵌入向量化调用事件
 *
 * <p>每次 Embedding 向量化完成后发布，携带文本长度、Token 估算等用量信息。 由应用装配层转发给 Governance 领域并记录用量。
 */
public class EmbeddingCallEvent {

    /**
     * model 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String model;
    /**
     * textLength 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final int textLength;
    /**
     * estimatedTokens 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final int estimatedTokens;
    /**
     * 向量数量，表示当前对象中的对应属性。
     */
    private final int vectorCount;
    /**
     * latencyMs 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final long latencyMs;

    /** 来源对象标识。 */
    private final String sourceId;

    /**
     * 租户标识，表示当前对象中的对应属性。
     */
    private final TenantId tenantId;
    /**
     * 用户标识，表示当前对象中的对应属性。
     */
    private final UserId userId;
    /**
     * correlationId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final CorrelationId correlationId;

    /**
     * {@code EmbeddingCallEvent} 创建并初始化当前类型实例。
     *
     * @param model 参数值，用于执行当前操作。
     * @param textLength 参数值，用于执行当前操作。
     * @param estimatedTokens 参数值，用于执行当前操作。
     * @param vectorCount 参数值，用于执行当前操作。
     * @param latencyMs 参数值，用于执行当前操作。
     */
    public EmbeddingCallEvent(
            String model, int textLength, int estimatedTokens, int vectorCount, long latencyMs) {
        this(model, textLength, estimatedTokens, vectorCount, latencyMs, null);
    }

    /**
     * {@code EmbeddingCallEvent} 创建并初始化当前类型实例。
     *
     * @param model 参数值，用于执行当前操作。
     * @param textLength 参数值，用于执行当前操作。
     * @param estimatedTokens 参数值，用于执行当前操作。
     * @param vectorCount 参数值，用于执行当前操作。
     * @param latencyMs 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     */
    public EmbeddingCallEvent(
            String model,
            int textLength,
            int estimatedTokens,
            int vectorCount,
            long latencyMs,
            TenantId tenantId) {
        this(model, textLength, estimatedTokens, vectorCount, latencyMs, tenantId, null);
    }

    /**
     * {@code EmbeddingCallEvent} 创建并初始化当前类型实例。
     *
     * @param model 参数值，用于执行当前操作。
     * @param textLength 参数值，用于执行当前操作。
     * @param estimatedTokens 参数值，用于执行当前操作。
     * @param vectorCount 参数值，用于执行当前操作。
     * @param latencyMs 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param userId 参数值，用于执行当前操作。
     */
    public EmbeddingCallEvent(
            String model,
            int textLength,
            int estimatedTokens,
            int vectorCount,
            long latencyMs,
            TenantId tenantId,
            UserId userId) {
        this(
                model,
                textLength,
                estimatedTokens,
                vectorCount,
                latencyMs,
                UUID.randomUUID().toString(),
                tenantId,
                userId);
    }

    /**
     * {@code EmbeddingCallEvent} 创建并初始化当前类型实例。
     *
     * @param model 参数值，用于执行当前操作。
     * @param textLength 参数值，用于执行当前操作。
     * @param estimatedTokens 参数值，用于执行当前操作。
     * @param vectorCount 参数值，用于执行当前操作。
     * @param latencyMs 参数值，用于执行当前操作。
     * @param sourceId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param userId 参数值，用于执行当前操作。
     */
    public EmbeddingCallEvent(
            String model,
            int textLength,
            int estimatedTokens,
            int vectorCount,
            long latencyMs,
            String sourceId,
            TenantId tenantId,
            UserId userId) {
        this(
                model,
                textLength,
                estimatedTokens,
                vectorCount,
                latencyMs,
                sourceId,
                tenantId,
                userId,
                CorrelationId.random());
    }

    /**
     * {@code EmbeddingCallEvent} 创建并初始化当前类型实例。
     *
     * @param model 参数值，用于执行当前操作。
     * @param textLength 参数值，用于执行当前操作。
     * @param estimatedTokens 参数值，用于执行当前操作。
     * @param vectorCount 参数值，用于执行当前操作。
     * @param latencyMs 参数值，用于执行当前操作。
     * @param sourceId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param userId 参数值，用于执行当前操作。
     * @param correlationId 参数值，用于执行当前操作。
     */
    public EmbeddingCallEvent(
            String model,
            int textLength,
            int estimatedTokens,
            int vectorCount,
            long latencyMs,
            String sourceId,
            TenantId tenantId,
            UserId userId,
            CorrelationId correlationId) {
        this.model = model;
        this.textLength = textLength;
        this.estimatedTokens = estimatedTokens;
        this.vectorCount = vectorCount;
        this.latencyMs = latencyMs;
        this.sourceId =
                sourceId == null || sourceId.isBlank() ? UUID.randomUUID().toString() : sourceId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.correlationId = correlationId == null ? CorrelationId.random() : correlationId;
    }

    /**
     * {@code getModel} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getModel() {
        return model;
    }

    /**
     * {@code getTextLength} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public int getTextLength() {
        return textLength;
    }

    /**
     * {@code getEstimatedTokens} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public int getEstimatedTokens() {
        return estimatedTokens;
    }

    /**
     * {@code getVectorCount} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public int getVectorCount() {
        return vectorCount;
    }

    /**
     * {@code getLatencyMs} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public long getLatencyMs() {
        return latencyMs;
    }

    /**
     * {@code getSourceId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * {@code getTenantId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public TenantId getTenantId() {
        return tenantId;
    }

    /**
     * {@code getUserId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public UserId getUserId() {
        return userId;
    }

    /**
     * {@code getCorrelationId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public CorrelationId getCorrelationId() {
        return correlationId;
    }
}
