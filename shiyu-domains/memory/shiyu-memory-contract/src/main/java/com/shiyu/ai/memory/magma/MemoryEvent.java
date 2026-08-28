package com.shiyu.ai.memory.magma;

import com.shiyu.ai.kernel.context.TenantId;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record MemoryEvent(
        String id,
        TenantId tenantId,
        String namespace,
        String subjectType,
        String subjectId,
        String eventType,
        String content,
        Instant occurredAt,
        String sourceType,
        String sourceId,
        Map<String, Object> attributes,
        double confidence,
        double importance,
        MemoryEventStatus status,
        ConfirmationPolicy confirmationPolicy,
        Instant createdAt,
        Instant updatedAt
) {
    public MemoryEvent {
        tenantId = Objects.requireNonNull(tenantId, "tenantId is required");
        if (id == null || id.isBlank()) throw new IllegalArgumentException("memory event id is required");
        if (namespace == null || namespace.isBlank()) throw new IllegalArgumentException("namespace is required");
        if (eventType == null || eventType.isBlank()) throw new IllegalArgumentException("eventType is required");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("content is required");
        if (status == null || confirmationPolicy == null) throw new IllegalArgumentException("event policy is required");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        confidence = clamp(confidence);
        importance = clamp(importance);
    }

    private static double clamp(double value) {
        return Math.max(0.0d, Math.min(1.0d, value));
    }
}
