package com.shiyu.ai.runtime;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import java.util.Map;

public record ContextQuery(TenantId tenantId, UserId ownerUserId, String namespace, String text,
                           int topK, Map<String, String> filters) {
    public ContextQuery {
        if (tenantId == null || ownerUserId == null) throw new IllegalArgumentException("tenant and owner are required");
        if (text == null || text.isBlank()) throw new IllegalArgumentException("context query is required");
        topK = topK <= 0 ? 5 : Math.min(topK, 50);
        filters = filters == null ? Map.of() : Map.copyOf(filters);
    }
}
