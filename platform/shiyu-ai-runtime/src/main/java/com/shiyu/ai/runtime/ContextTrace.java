package com.shiyu.ai.runtime;

import java.time.Instant;
import java.util.List;

public record ContextTrace(String id, long tenantId, String query, List<String> itemIds,
                           String policy, Instant createdAt) {
    public ContextTrace {
        itemIds = itemIds == null ? List.of() : List.copyOf(itemIds);
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
