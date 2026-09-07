package com.shiyu.ai.model.event;

import com.shiyu.ai.kernel.context.CorrelationId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import java.util.UUID;

/**
 * 模型调用事件
 * <p>
 * 每次 LLM 对话调用完成后发布，携带 Token 用量、平台、模型等信息。
 * 由应用装配层转发给 Governance 领域并记录用量。
 * </p>
 */
public class ModelCallEvent {

    private final String platform;
    private final String model;
    private final int promptTokens;
    private final int completionTokens;
    private final long latencyMs;
    private final String generationRunId;
    /** Stable id for this call, reused if the event is delivered more than once. */
    private final String sourceId;
    private final TenantId tenantId;
    private final UserId userId;
    private final CorrelationId correlationId;

    public ModelCallEvent(String platform, String model,
                          int promptTokens, int completionTokens, long latencyMs) {
        this(platform, model, promptTokens, completionTokens, latencyMs, null);
    }

    public ModelCallEvent(String platform, String model,
                          int promptTokens, int completionTokens, long latencyMs, String generationRunId) {
        this(platform, model, promptTokens, completionTokens, latencyMs, generationRunId, null);
    }

    public ModelCallEvent(String platform, String model,
                          int promptTokens, int completionTokens, long latencyMs,
                          String generationRunId, TenantId tenantId) {
        this(platform, model, promptTokens, completionTokens, latencyMs, generationRunId, tenantId, null);
    }

    public ModelCallEvent(String platform, String model,
                          int promptTokens, int completionTokens, long latencyMs,
                          String generationRunId, TenantId tenantId, UserId userId) {
        this(platform, model, promptTokens, completionTokens, latencyMs,
                generationRunId, UUID.randomUUID().toString(), tenantId, userId);
    }

    public ModelCallEvent(String platform, String model,
                          int promptTokens, int completionTokens, long latencyMs,
                          String generationRunId, String sourceId, TenantId tenantId, UserId userId) {
        this(platform, model, promptTokens, completionTokens, latencyMs,
                generationRunId, sourceId, tenantId, userId, CorrelationId.random());
    }

    public ModelCallEvent(String platform, String model,
                          int promptTokens, int completionTokens, long latencyMs,
                          String generationRunId, String sourceId, TenantId tenantId, UserId userId,
                          CorrelationId correlationId) {
        this.platform = platform;
        this.model = model;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.latencyMs = latencyMs;
        this.generationRunId = generationRunId;
        this.sourceId = sourceId == null || sourceId.isBlank() ? UUID.randomUUID().toString() : sourceId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.correlationId = correlationId == null ? CorrelationId.random() : correlationId;
    }

    public String getPlatform() { return platform; }
    public String getModel() { return model; }
    public int getPromptTokens() { return promptTokens; }
    public int getCompletionTokens() { return completionTokens; }
    public int getTotalTokens() { return promptTokens + completionTokens; }
    public long getLatencyMs() { return latencyMs; }
    public String getGenerationRunId() { return generationRunId; }
    public String getSourceId() { return sourceId; }
    public TenantId getTenantId() { return tenantId; }
    public UserId getUserId() { return userId; }
    public CorrelationId getCorrelationId() { return correlationId; }
}
