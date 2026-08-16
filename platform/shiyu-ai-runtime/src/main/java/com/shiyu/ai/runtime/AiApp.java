package com.shiyu.ai.runtime;

import java.time.Instant;

public record AiApp(String id, long tenantId, long ownerUserId, String name, String description,
                    String status, String publishedVersionId, Instant createdAt, Instant updatedAt) {
    public AiApp {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("app id is required");
        if (tenantId <= 0 || ownerUserId <= 0) throw new IllegalArgumentException("tenant and owner are required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("app name is required");
        status = status == null || status.isBlank() ? "ACTIVE" : status;
        createdAt = createdAt == null ? Instant.now() : createdAt;
        updatedAt = updatedAt == null ? createdAt : updatedAt;
    }
}
