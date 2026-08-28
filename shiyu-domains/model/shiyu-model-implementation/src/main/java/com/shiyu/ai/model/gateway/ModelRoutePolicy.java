package com.shiyu.ai.model.gateway;

import java.util.List;

public record ModelRoutePolicy(String id, long tenantId, String name, List<String> orderedModels,
                               int timeoutMs, boolean fallbackOnError, long maxTokens) {
    public ModelRoutePolicy {
        if (id == null || id.isBlank() || tenantId <= 0 || name == null || name.isBlank()) throw new IllegalArgumentException("route identity is required");
        orderedModels = orderedModels == null ? List.of() : orderedModels.stream().filter(model -> model != null && !model.isBlank()).map(String::trim).toList();
        if (orderedModels.isEmpty()) throw new IllegalArgumentException("at least one model is required");
        timeoutMs = timeoutMs <= 0 ? 30_000 : Math.min(timeoutMs, 300_000);
        maxTokens = maxTokens <= 0 ? 16_000 : Math.min(maxTokens, 128_000);
    }
}
