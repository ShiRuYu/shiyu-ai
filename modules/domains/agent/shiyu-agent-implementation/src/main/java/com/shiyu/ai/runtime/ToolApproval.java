package com.shiyu.ai.runtime;

import java.time.Instant;

public record ToolApproval(String id, String runId, long tenantId, long ownerUserId, String toolName,
                           String argumentsRedacted, ToolApprovalStatus status, Instant createdAt, Instant decidedAt,
                           Instant expiresAt) {
    public ToolApproval(String id, String runId, long tenantId, long ownerUserId, String toolName,
                        String argumentsRedacted, ToolApprovalStatus status, Instant createdAt, Instant decidedAt) {
        this(id, runId, tenantId, ownerUserId, toolName, argumentsRedacted, status, createdAt, decidedAt,
                (createdAt == null ? Instant.now() : createdAt).plusSeconds(300));
    }
    public ToolApproval {
        if (id == null || id.isBlank() || runId == null || runId.isBlank() || tenantId <= 0 || ownerUserId <= 0 || toolName == null || toolName.isBlank()) throw new IllegalArgumentException("approval identity is required");
        argumentsRedacted = argumentsRedacted == null ? "{}" : argumentsRedacted;
        status = status == null ? ToolApprovalStatus.PENDING : status;
        createdAt = createdAt == null ? Instant.now() : createdAt;
        expiresAt = expiresAt == null ? createdAt.plusSeconds(300) : expiresAt;
        if (expiresAt.isBefore(createdAt)) throw new IllegalArgumentException("approval expiry cannot precede creation");
    }
}
