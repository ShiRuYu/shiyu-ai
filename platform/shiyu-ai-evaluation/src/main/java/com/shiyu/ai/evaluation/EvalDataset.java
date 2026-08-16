package com.shiyu.ai.evaluation;

import java.time.Instant;

public record EvalDataset(String id, long tenantId, long ownerUserId, String name, String description, Instant createdAt) {
    public EvalDataset {
        if (id == null || id.isBlank() || tenantId <= 0 || ownerUserId <= 0) throw new IllegalArgumentException("dataset identity is required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("dataset name is required");
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
