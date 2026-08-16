package com.shiyu.ai.memory.magma;

import java.util.Map;

public record MemoryEntity(
        String id,
        long tenantId,
        String entityType,
        String externalRef,
        String displayName,
        String normalizedName,
        Map<String, Object> attributes,
        boolean active
) {
    public MemoryEntity {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("entity id is required");
        if (entityType == null || entityType.isBlank()) throw new IllegalArgumentException("entityType is required");
        if (externalRef == null || externalRef.isBlank()) throw new IllegalArgumentException("externalRef is required");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
