package com.shiyu.ai.runtime;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.Map;

public record AiRunContext(TenantId tenantId, long ownerUserId, String appId, String appVersionId,
                           String conversationId, String generationId, String executionId,
                           String traceId, Map<String, String> attributes) {
    public AiRunContext {
        if (tenantId == null || tenantId.value() <= 0 || ownerUserId <= 0) {
            throw new IllegalArgumentException("tenant and owner are required");
        }
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
