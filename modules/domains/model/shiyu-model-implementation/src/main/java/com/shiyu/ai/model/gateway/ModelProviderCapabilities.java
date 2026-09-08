package com.shiyu.ai.model.gateway;

import java.util.Set;
import java.util.List;

public record ModelProviderCapabilities(String provider, String model, Set<String> features, int contextWindow,
                                        boolean streaming, boolean tools, boolean parallelTools,
                                        boolean multimodal, boolean jsonSchema, List<String> reasoningLevels,
                                        int maxOutputTokens, boolean streamUsage, boolean cacheUsage,
                                        boolean cancellation) {
    public ModelProviderCapabilities(String provider, String model, Set<String> features, int contextWindow) {
        this(provider, model, features, contextWindow, true, features != null && features.contains("tool_calls"),
                features != null && features.contains("parallel_tool_calls"), false,
                features != null && features.contains("structured"), List.of(), 4096, true, false, true);
    }

    public ModelProviderCapabilities {
        if (provider == null || provider.isBlank() || model == null || model.isBlank()) throw new IllegalArgumentException("provider and model are required");
        features = features == null ? Set.of("chat") : Set.copyOf(features);
        contextWindow = contextWindow <= 0 ? 8192 : contextWindow;
        reasoningLevels = reasoningLevels == null ? List.of() : List.copyOf(reasoningLevels);
        maxOutputTokens = maxOutputTokens <= 0 ? 4096 : maxOutputTokens;
    }

    public boolean supports(String feature) { return feature != null && features.contains(feature); }
}
