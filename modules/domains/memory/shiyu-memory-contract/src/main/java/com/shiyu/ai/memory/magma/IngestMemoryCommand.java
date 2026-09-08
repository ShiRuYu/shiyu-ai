package com.shiyu.ai.memory.magma;

import com.shiyu.ai.kernel.context.TenantId;
import java.time.Instant;
import java.util.Map;

public record IngestMemoryCommand(
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
        ConfirmationPolicy confirmationPolicy
) {
    public IngestMemoryCommand {
        if (tenantId == null) throw new IllegalArgumentException("tenantId is required");
        if (namespace == null || namespace.isBlank()) throw new IllegalArgumentException("namespace is required");
        if (subjectType == null || subjectType.isBlank()) throw new IllegalArgumentException("subjectType is required");
        if (subjectId == null || subjectId.isBlank()) throw new IllegalArgumentException("subjectId is required");
        if (eventType == null || eventType.isBlank()) throw new IllegalArgumentException("eventType is required");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("content is required");
        if (occurredAt == null) occurredAt = Instant.now();
        if (confirmationPolicy == null) confirmationPolicy = ConfirmationPolicy.REQUIRED;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
