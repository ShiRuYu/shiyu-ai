package com.shiyu.ai.runtime;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import java.time.Instant;
import java.util.Objects;

public record AiApp(String id, TenantId tenantId, UserId ownerUserId, String name, String description,
                    String status, String publishedVersionId, Instant createdAt, Instant updatedAt) {
    public AiApp {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("app id is required");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(ownerUserId, "ownerUserId must not be null");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("app name is required");
        status = status == null || status.isBlank() ? "ACTIVE" : status;
        createdAt = createdAt == null ? Instant.now() : createdAt;
        updatedAt = updatedAt == null ? createdAt : updatedAt;
    }
}
