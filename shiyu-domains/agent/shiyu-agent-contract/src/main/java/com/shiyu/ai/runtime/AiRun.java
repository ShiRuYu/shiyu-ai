package com.shiyu.ai.runtime;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import java.time.Instant;
import java.util.Objects;

public record AiRun(String id, TenantId tenantId, UserId ownerUserId, String appId, String appVersionId,
                    AiRunSource sourceType, String sourceId, String parentRunId, String traceId,
                    String conversationId, String generationId, String executionId, String model,
                    String promptHash, AiRunStatus status, long promptTokens, long completionTokens,
                    boolean estimatedUsage, String costSnapshot, Instant createdAt, Instant completedAt,
                    String errorCode, long version, long lastEventSeq) {
    public AiRun(String id, TenantId tenantId, UserId ownerUserId, String appId, String appVersionId,
                 AiRunSource sourceType, String sourceId, String parentRunId, String traceId,
                 String conversationId, String generationId, String executionId, String model,
                 String promptHash, AiRunStatus status, long promptTokens, long completionTokens,
                 boolean estimatedUsage, String costSnapshot, Instant createdAt, Instant completedAt,
                 String errorCode, long version) {
        this(id, tenantId, ownerUserId, appId, appVersionId, sourceType, sourceId, parentRunId, traceId,
                conversationId, generationId, executionId, model, promptHash, status, promptTokens,
                completionTokens, estimatedUsage, costSnapshot, createdAt, completedAt, errorCode, version, 0);
    }

    public AiRun {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("run id is required");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(ownerUserId, "ownerUserId must not be null");
        if (sourceType == null || sourceId == null || sourceId.isBlank()) throw new IllegalArgumentException("run source is required");
        status = status == null ? AiRunStatus.CREATED : status;
        promptTokens = Math.max(0, promptTokens);
        completionTokens = Math.max(0, completionTokens);
        createdAt = createdAt == null ? Instant.now() : createdAt;
        lastEventSeq = Math.max(0, lastEventSeq);
    }

    public AiRun transition(AiRunStatus next) {
        if (next == null) throw new IllegalArgumentException("next status is required");
        if (status == AiRunStatus.CREATED && next != AiRunStatus.RUNNING && next != AiRunStatus.CANCELLED)
            throw new IllegalStateException("created run can only start or cancel");
        if (status == AiRunStatus.RUNNING && next != AiRunStatus.COMPLETED && next != AiRunStatus.FAILED && next != AiRunStatus.CANCELLED)
            throw new IllegalStateException("running run can only finish");
        if (status == AiRunStatus.COMPLETED || status == AiRunStatus.FAILED || status == AiRunStatus.CANCELLED)
            throw new IllegalStateException("terminal run cannot transition");
        return new AiRun(id, tenantId, ownerUserId, appId, appVersionId, sourceType, sourceId, parentRunId, traceId,
                conversationId, generationId, executionId, model, promptHash, next, promptTokens, completionTokens,
                estimatedUsage, costSnapshot, createdAt, next == AiRunStatus.RUNNING ? completedAt : Instant.now(), errorCode, version + 1, lastEventSeq);
    }

    public AiRun withLastEventSeq(long sequence) {
        return new AiRun(id, tenantId, ownerUserId, appId, appVersionId, sourceType, sourceId, parentRunId, traceId,
                conversationId, generationId, executionId, model, promptHash, status, promptTokens, completionTokens,
                estimatedUsage, costSnapshot, createdAt, completedAt, errorCode, version, Math.max(0, sequence));
    }
}
