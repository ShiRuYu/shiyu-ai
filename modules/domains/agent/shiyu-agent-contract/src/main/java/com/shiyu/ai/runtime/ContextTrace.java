package com.shiyu.ai.runtime;

import com.shiyu.ai.kernel.context.TenantId;

import java.time.Instant;
import java.util.List;

public record ContextTrace(String id, TenantId tenantId, String query, List<String> itemIds,
                           String policy, Instant createdAt) {
    public ContextTrace {
        if (tenantId == null) throw new IllegalArgumentException("tenant is required");
        itemIds = itemIds == null ? List.of() : List.copyOf(itemIds);
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
