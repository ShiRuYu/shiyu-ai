package com.shiyu.ai.agent.contract.runtime;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import java.util.Map;

/** Tenant- and user-scoped query used to assemble agent context. */
public record ContextQuery(
        TenantId tenantId,
        UserId ownerUserId,
        String namespace,
        String text,
        int topK,
        Map<String, String> filters) {
    public ContextQuery {
        if (tenantId == null || ownerUserId == null)
            throw new IllegalArgumentException("tenant and owner are required");
        if (text == null || text.isBlank())
            throw new IllegalArgumentException("context query is required");
        topK = topK <= 0 ? 5 : Math.min(topK, 50);
        filters = filters == null ? Map.of() : Map.copyOf(filters);
    }
}
