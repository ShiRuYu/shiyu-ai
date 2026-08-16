package com.shiyu.ai.runtime;

import java.util.Map;

public record ContextQuery(long tenantId, long ownerUserId, String namespace, String text,
                           int topK, Map<String, String> filters) {
    public ContextQuery {
        if (tenantId <= 0 || ownerUserId <= 0) throw new IllegalArgumentException("tenant and owner are required");
        if (text == null || text.isBlank()) throw new IllegalArgumentException("context query is required");
        topK = topK <= 0 ? 5 : Math.min(topK, 50);
        filters = filters == null ? Map.of() : Map.copyOf(filters);
    }
}
