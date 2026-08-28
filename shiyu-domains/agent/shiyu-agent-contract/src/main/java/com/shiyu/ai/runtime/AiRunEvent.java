package com.shiyu.ai.runtime;

import com.shiyu.ai.kernel.context.TenantId;
import java.time.Instant;
import java.util.Objects;

public record AiRunEvent(String runId, TenantId tenantId, long seq, AiRunEventType type,
                         int schemaVersion, String turnId, String stepId, Long parentEventSeq,
                         String conversationId, String generationId, String executionId,
                         String appId, String appVersionId, String providerRequestId, String traceId,
                         String payload, boolean redacted, Instant createdAt) {
    /** Backward-compatible constructor for callers that only provide the event envelope. */
    public AiRunEvent(String runId, TenantId tenantId, long seq, AiRunEventType type, String payload,
                      boolean redacted, Instant createdAt) {
        this(runId, tenantId, seq, type, 1, null, null, seq > 1 ? seq - 1 : null,
                null, null, null, null, null, null, null, payload, redacted, createdAt);
    }

    public AiRunEvent {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        if (runId == null || runId.isBlank() || seq <= 0 || type == null)
            throw new IllegalArgumentException("run event identity is required");
        if (schemaVersion <= 0) schemaVersion = 1;
        if (parentEventSeq != null && (parentEventSeq <= 0 || parentEventSeq >= seq)) {
            throw new IllegalArgumentException("parent event sequence must precede the event");
        }
        payload = payload == null ? "{}" : payload;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
