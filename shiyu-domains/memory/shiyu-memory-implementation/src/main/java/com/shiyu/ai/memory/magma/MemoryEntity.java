package com.shiyu.ai.memory.magma;

import com.shiyu.ai.kernel.context.TenantId;
import java.util.Map;
import java.util.Objects;

public record MemoryEntity(
        String id,
        TenantId tenantId,
        String entityType,
        String externalRef,
        String displayName,
        String normalizedName,
        Map<String, Object> attributes,
        boolean active
) {
    public MemoryEntity {
        tenantId = Objects.requireNonNull(tenantId, "tenantId is required");
        if (id == null || id.isBlank()) throw new IllegalArgumentException("entity id is required");
        if (entityType == null || entityType.isBlank()) throw new IllegalArgumentException("entityType is required");
        if (externalRef == null || externalRef.isBlank()) throw new IllegalArgumentException("externalRef is required");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
