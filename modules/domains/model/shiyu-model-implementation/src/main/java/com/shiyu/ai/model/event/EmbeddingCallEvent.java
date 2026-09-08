package com.shiyu.ai.model.event;

import com.shiyu.ai.kernel.context.CorrelationId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import java.util.UUID;

/**
 * 嵌入向量化调用事件
 * <p>
 * 每次 Embedding 向量化完成后发布，携带文本长度、Token 估算等用量信息。
 * 由应用装配层转发给 Governance 领域并记录用量。
 * </p>
 */
public class EmbeddingCallEvent {

    private final String model;
    private final int textLength;
    private final int estimatedTokens;
    private final int vectorCount;
    private final long latencyMs;
    /** Stable id for this embedding operation, reused on event redelivery. */
    private final String sourceId;
    private final TenantId tenantId;
    private final UserId userId;
    private final CorrelationId correlationId;

    public EmbeddingCallEvent(String model, int textLength,
                              int estimatedTokens, int vectorCount, long latencyMs) {
        this(model, textLength, estimatedTokens, vectorCount, latencyMs, null);
    }

    public EmbeddingCallEvent(String model, int textLength,
                              int estimatedTokens, int vectorCount, long latencyMs, TenantId tenantId) {
        this(model, textLength, estimatedTokens, vectorCount, latencyMs, tenantId, null);
    }

    public EmbeddingCallEvent(String model, int textLength,
                              int estimatedTokens, int vectorCount, long latencyMs,
                              TenantId tenantId, UserId userId) {
        this(model, textLength, estimatedTokens, vectorCount, latencyMs,
                UUID.randomUUID().toString(), tenantId, userId);
    }

    public EmbeddingCallEvent(String model, int textLength,
                              int estimatedTokens, int vectorCount, long latencyMs,
                              String sourceId, TenantId tenantId, UserId userId) {
        this(model, textLength, estimatedTokens, vectorCount, latencyMs,
                sourceId, tenantId, userId, CorrelationId.random());
    }

    public EmbeddingCallEvent(String model, int textLength,
                              int estimatedTokens, int vectorCount, long latencyMs,
                              String sourceId, TenantId tenantId, UserId userId,
                              CorrelationId correlationId) {
        this.model = model;
        this.textLength = textLength;
        this.estimatedTokens = estimatedTokens;
        this.vectorCount = vectorCount;
        this.latencyMs = latencyMs;
        this.sourceId = sourceId == null || sourceId.isBlank() ? UUID.randomUUID().toString() : sourceId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.correlationId = correlationId == null ? CorrelationId.random() : correlationId;
    }

    public String getModel() { return model; }
    public int getTextLength() { return textLength; }
    public int getEstimatedTokens() { return estimatedTokens; }
    public int getVectorCount() { return vectorCount; }
    public long getLatencyMs() { return latencyMs; }
    public String getSourceId() { return sourceId; }
    public TenantId getTenantId() { return tenantId; }
    public UserId getUserId() { return userId; }
    public CorrelationId getCorrelationId() { return correlationId; }
}
