package com.shiyu.ai.runtime;

import java.time.Instant;

public record ToolApproval(String id, String runId, long tenantId, long ownerUserId, String toolName,
                           String argumentsRedacted, ToolApprovalStatus status, Instant createdAt, Instant decidedAt) {
    public ToolApproval {
        if (id == null || id.isBlank() || runId == null || runId.isBlank() || tenantId <= 0 || ownerUserId <= 0 || toolName == null || toolName.isBlank()) throw new IllegalArgumentException("approval identity is required");
        argumentsRedacted = argumentsRedacted == null ? "{}" : argumentsRedacted;
        status = status == null ? ToolApprovalStatus.PENDING : status;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
