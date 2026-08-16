package com.shiyu.ai.runtime;

import java.time.Instant;

public record AiAppVersion(String id, String appId, long tenantId, String version, String configJson,
                           String status, Instant createdAt, Instant publishedAt) {
    public AiAppVersion {
        if (id == null || id.isBlank() || appId == null || appId.isBlank()) throw new IllegalArgumentException("app version identity is required");
        if (tenantId <= 0) throw new IllegalArgumentException("tenant is required");
        if (version == null || version.isBlank()) throw new IllegalArgumentException("version is required");
        configJson = configJson == null ? "{}" : configJson;
        status = status == null || status.isBlank() ? "DRAFT" : status;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public boolean published() { return "PUBLISHED".equals(status); }
}
