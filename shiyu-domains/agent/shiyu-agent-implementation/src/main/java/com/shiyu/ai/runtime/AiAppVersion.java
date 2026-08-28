package com.shiyu.ai.runtime;

import com.shiyu.ai.kernel.context.TenantId;
import java.time.Instant;
import java.util.Objects;

public record AiAppVersion(String id, String appId, TenantId tenantId, String version, String configJson,
                           String status, Instant createdAt, Instant publishedAt) {
    public AiAppVersion {
        if (id == null || id.isBlank() || appId == null || appId.isBlank()) throw new IllegalArgumentException("app version identity is required");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        if (version == null || version.isBlank()) throw new IllegalArgumentException("version is required");
        configJson = configJson == null ? "{}" : configJson;
        status = status == null || status.isBlank() ? "DRAFT" : status;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public boolean published() { return "PUBLISHED".equals(status); }
}
