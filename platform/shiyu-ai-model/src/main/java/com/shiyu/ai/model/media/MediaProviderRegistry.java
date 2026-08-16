package com.shiyu.ai.model.media;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MediaProviderRegistry {
    private final List<MediaProvider> providers;
    public MediaProviderRegistry(List<MediaProvider> providers) { this.providers = providers == null ? List.of() : List.copyOf(providers); }
    public List<MediaProvider> providers() { return providers; }
    public MediaProvider require() { return providers.stream().findFirst().orElseThrow(() -> new IllegalStateException("no media provider configured")); }
    public MediaProvider require(String id) {
        if (id == null || id.isBlank()) return require();
        return providers.stream().filter(provider -> id.equals(provider.id())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("media provider not found: " + id));
    }
}
