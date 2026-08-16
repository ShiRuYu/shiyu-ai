package com.shiyu.ai.model.gateway;

import java.util.Set;

public record ModelProviderCapabilities(String provider, String model, Set<String> features, int contextWindow) {
    public ModelProviderCapabilities {
        if (provider == null || provider.isBlank() || model == null || model.isBlank()) throw new IllegalArgumentException("provider and model are required");
        features = features == null ? Set.of("chat") : Set.copyOf(features);
        contextWindow = contextWindow <= 0 ? 8192 : contextWindow;
    }
}
