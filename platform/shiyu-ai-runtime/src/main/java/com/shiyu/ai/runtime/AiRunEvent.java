package com.shiyu.ai.runtime;

import java.time.Instant;

public record AiRunEvent(String runId, long tenantId, long seq, AiRunEventType type, String payload,
                         boolean redacted, Instant createdAt) {
    public AiRunEvent {
        if (runId == null || runId.isBlank() || tenantId <= 0 || seq <= 0 || type == null)
            throw new IllegalArgumentException("run event identity is required");
        payload = payload == null ? "{}" : payload;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
