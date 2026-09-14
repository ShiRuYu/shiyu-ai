package com.shiyu.ai.agent.implementation.runtime.model;

import com.shiyu.ai.agent.contract.runtime.*;

import java.time.Instant;

/**
 * {@code ToolApproval} 封装智能体模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param runId 运行标识，表示该记录组件承载的数据。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param ownerUserId 所属用户标识，表示该记录组件承载的数据。
 * @param toolName toolName 属性，表示该记录组件承载的数据。
 * @param argumentsRedacted argumentsRedacted 属性，表示该记录组件承载的数据。
 * @param status 状态，表示该记录组件承载的数据。
 * @param createdAt 创建时间，表示该记录组件承载的数据。
 * @param decidedAt decidedAt 属性，表示该记录组件承载的数据。
 * @param expiresAt 过期时间，表示该记录组件承载的数据。
 */
public record ToolApproval(
        String id,
        String runId,
        long tenantId,
        long ownerUserId,
        String toolName,
        String argumentsRedacted,
        ToolApprovalStatus status,
        Instant createdAt,
        Instant decidedAt,
        Instant expiresAt) {
    public ToolApproval(
            String id,
            String runId,
            long tenantId,
            long ownerUserId,
            String toolName,
            String argumentsRedacted,
            ToolApprovalStatus status,
            Instant createdAt,
            Instant decidedAt) {
        this(
                id,
                runId,
                tenantId,
                ownerUserId,
                toolName,
                argumentsRedacted,
                status,
                createdAt,
                decidedAt,
                (createdAt == null ? Instant.now() : createdAt).plusSeconds(300));
    }

    public ToolApproval {
        if (id == null
                || id.isBlank()
                || runId == null
                || runId.isBlank()
                || tenantId <= 0
                || ownerUserId <= 0
                || toolName == null
                || toolName.isBlank())
            throw new IllegalArgumentException("approval identity is required");
        argumentsRedacted = argumentsRedacted == null ? "{}" : argumentsRedacted;
        status = status == null ? ToolApprovalStatus.PENDING : status;
        createdAt = createdAt == null ? Instant.now() : createdAt;
        expiresAt = expiresAt == null ? createdAt.plusSeconds(300) : expiresAt;
        if (expiresAt.isBefore(createdAt))
            throw new IllegalArgumentException("approval expiry cannot precede creation");
    }
}
