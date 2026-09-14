package com.shiyu.ai.agent.contract.runtime;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * {@code AiRun} 封装智能体模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param ownerUserId 所属用户标识，表示该记录组件承载的数据。
 * @param appId appId 属性，表示该记录组件承载的数据。
 * @param appVersionId appVersionId 属性，表示该记录组件承载的数据。
 * @param sourceType sourceType 属性，表示该记录组件承载的数据。
 * @param sourceId sourceId 属性，表示该记录组件承载的数据。
 * @param parentRunId parentRunId 属性，表示该记录组件承载的数据。
 * @param traceId traceId 属性，表示该记录组件承载的数据。
 * @param conversationId conversationId 属性，表示该记录组件承载的数据。
 * @param generationId generationId 属性，表示该记录组件承载的数据。
 * @param executionId executionId 属性，表示该记录组件承载的数据。
 * @param model 模型，表示该记录组件承载的数据。
 * @param promptHash promptHash 属性，表示该记录组件承载的数据。
 * @param status 状态，表示该记录组件承载的数据。
 * @param promptTokens promptTokens 属性，表示该记录组件承载的数据。
 * @param completionTokens completionTokens 属性，表示该记录组件承载的数据。
 * @param estimatedUsage estimatedUsage 属性，表示该记录组件承载的数据。
 * @param costSnapshot costSnapshot 属性，表示该记录组件承载的数据。
 * @param createdAt 创建时间，表示该记录组件承载的数据。
 * @param completedAt completedAt 属性，表示该记录组件承载的数据。
 * @param errorCode errorCode 属性，表示该记录组件承载的数据。
 * @param version version 属性，表示该记录组件承载的数据。
 * @param lastEventSeq lastEventSeq 属性，表示该记录组件承载的数据。
 */
public record AiRun(
        String id,
        TenantId tenantId,
        UserId ownerUserId,
        String appId,
        String appVersionId,
        AiRunSource sourceType,
        String sourceId,
        String parentRunId,
        String traceId,
        String conversationId,
        String generationId,
        String executionId,
        String model,
        String promptHash,
        AiRunStatus status,
        long promptTokens,
        long completionTokens,
        boolean estimatedUsage,
        String costSnapshot,
        Instant createdAt,
        Instant completedAt,
        String errorCode,
        long version,
        long lastEventSeq) {
    public AiRun(
            String id,
            TenantId tenantId,
            UserId ownerUserId,
            String appId,
            String appVersionId,
            AiRunSource sourceType,
            String sourceId,
            String parentRunId,
            String traceId,
            String conversationId,
            String generationId,
            String executionId,
            String model,
            String promptHash,
            AiRunStatus status,
            long promptTokens,
            long completionTokens,
            boolean estimatedUsage,
            String costSnapshot,
            Instant createdAt,
            Instant completedAt,
            String errorCode,
            long version) {
        this(
                id,
                tenantId,
                ownerUserId,
                appId,
                appVersionId,
                sourceType,
                sourceId,
                parentRunId,
                traceId,
                conversationId,
                generationId,
                executionId,
                model,
                promptHash,
                status,
                promptTokens,
                completionTokens,
                estimatedUsage,
                costSnapshot,
                createdAt,
                completedAt,
                errorCode,
                version,
                0);
    }

    public AiRun {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("run id is required");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(ownerUserId, "ownerUserId must not be null");
        if (sourceType == null || sourceId == null || sourceId.isBlank())
            throw new IllegalArgumentException("run source is required");
        status = status == null ? AiRunStatus.CREATED : status;
        promptTokens = Math.max(0, promptTokens);
        completionTokens = Math.max(0, completionTokens);
        createdAt = createdAt == null ? Instant.now() : createdAt;
        lastEventSeq = Math.max(0, lastEventSeq);
    }

    public AiRun transition(AiRunStatus next) {
        if (next == null) throw new IllegalArgumentException("next status is required");
        if (status == AiRunStatus.CREATED
                && next != AiRunStatus.RUNNING
                && next != AiRunStatus.CANCELLED)
            throw new IllegalStateException("created run can only start or cancel");
        if (status == AiRunStatus.RUNNING
                && next != AiRunStatus.COMPLETED
                && next != AiRunStatus.FAILED
                && next != AiRunStatus.CANCELLED)
            throw new IllegalStateException("running run can only finish");
        if (status == AiRunStatus.COMPLETED
                || status == AiRunStatus.FAILED
                || status == AiRunStatus.CANCELLED)
            throw new IllegalStateException("terminal run cannot transition");
        return new AiRun(
                id,
                tenantId,
                ownerUserId,
                appId,
                appVersionId,
                sourceType,
                sourceId,
                parentRunId,
                traceId,
                conversationId,
                generationId,
                executionId,
                model,
                promptHash,
                next,
                promptTokens,
                completionTokens,
                estimatedUsage,
                costSnapshot,
                createdAt,
                next == AiRunStatus.RUNNING ? completedAt : Instant.now(),
                errorCode,
                version + 1,
                lastEventSeq);
    }

    public AiRun withLastEventSeq(long sequence) {
        return new AiRun(
                id,
                tenantId,
                ownerUserId,
                appId,
                appVersionId,
                sourceType,
                sourceId,
                parentRunId,
                traceId,
                conversationId,
                generationId,
                executionId,
                model,
                promptHash,
                status,
                promptTokens,
                completionTokens,
                estimatedUsage,
                costSnapshot,
                createdAt,
                completedAt,
                errorCode,
                version,
                Math.max(0, sequence));
    }
}
