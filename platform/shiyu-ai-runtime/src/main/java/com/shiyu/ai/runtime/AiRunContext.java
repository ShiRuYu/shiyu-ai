package com.shiyu.ai.runtime;

import java.util.Map;

public record AiRunContext(long tenantId, long ownerUserId, String appId, String appVersionId,
                           String conversationId, String generationId, String executionId,
                           String traceId, Map<String, String> attributes) {
    public AiRunContext {
        if (tenantId <= 0 || ownerUserId <= 0) throw new IllegalArgumentException("tenant and owner are required");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
