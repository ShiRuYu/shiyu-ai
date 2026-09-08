package com.shiyu.ai.plugin.market;

import java.time.Instant;
import java.util.List;

/** Signed, auditable catalog metadata. The package itself is never loaded into the host classloader. */
public record PluginMarketEntry(String id, String version, String source, String manifest,
                                String signature, String publisherKey, List<String> permissions,
                                String checksum, String updatePolicy, Instant publishedAt, boolean enabled) {
    public PluginMarketEntry(String id, String version, String source, String manifest,
                              String signature, String publisherKey, List<String> permissions,
                              Instant publishedAt, boolean enabled) {
        this(id, version, source, manifest, signature, publisherKey, permissions, null, "MANUAL", publishedAt, enabled);
    }
    public PluginMarketEntry {
        if (id == null || id.isBlank() || version == null || version.isBlank()) throw new IllegalArgumentException("plugin id/version required");
        if (manifest == null || manifest.isBlank()) throw new IllegalArgumentException("plugin manifest is required");
        permissions = permissions == null ? List.of() : List.copyOf(permissions);
        updatePolicy = updatePolicy == null || updatePolicy.isBlank() ? "MANUAL" : updatePolicy;
    }
}
