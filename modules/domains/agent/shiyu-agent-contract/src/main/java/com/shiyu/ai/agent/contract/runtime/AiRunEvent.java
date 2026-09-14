package com.shiyu.ai.agent.contract.runtime;

import com.shiyu.ai.kernel.context.TenantId;

import java.time.Instant;
import java.util.Objects;

/**
 * {@code AiRunEvent} 封装智能体模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param runId 运行标识，表示该记录组件承载的数据。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param seq seq 属性，表示该记录组件承载的数据。
 * @param type 类型，表示该记录组件承载的数据。
 * @param schemaVersion schemaVersion 属性，表示该记录组件承载的数据。
 * @param turnId turnId 属性，表示该记录组件承载的数据。
 * @param stepId stepId 属性，表示该记录组件承载的数据。
 * @param parentEventSeq parentEventSeq 属性，表示该记录组件承载的数据。
 * @param conversationId conversationId 属性，表示该记录组件承载的数据。
 * @param generationId generationId 属性，表示该记录组件承载的数据。
 * @param executionId executionId 属性，表示该记录组件承载的数据。
 * @param appId appId 属性，表示该记录组件承载的数据。
 * @param appVersionId appVersionId 属性，表示该记录组件承载的数据。
 * @param providerRequestId providerRequestId 属性，表示该记录组件承载的数据。
 * @param traceId traceId 属性，表示该记录组件承载的数据。
 * @param payload payload 属性，表示该记录组件承载的数据。
 * @param redacted redacted 属性，表示该记录组件承载的数据。
 * @param createdAt 创建时间，表示该记录组件承载的数据。
 */
public record AiRunEvent(
        String runId,
        TenantId tenantId,
        long seq,
        AiRunEventType type,
        int schemaVersion,
        String turnId,
        String stepId,
        Long parentEventSeq,
        String conversationId,
        String generationId,
        String executionId,
        String appId,
        String appVersionId,
        String providerRequestId,
        String traceId,
        String payload,
        boolean redacted,
        Instant createdAt) {
    /**
     * 处理ai运行事件。
     *
     * @param runId 运行记录标识。
     * @param tenantId 租户标识。
     * @param seq seq 参数。
     * @param type 数据类型。
     * @param payload 事件载荷。
     * @param redacted redacted 参数。
     * @param createdAt createdAt 参数。
     *
     * @return 处理结果。
     */
    public AiRunEvent(
            String runId,
            TenantId tenantId,
            long seq,
            AiRunEventType type,
            String payload,
            boolean redacted,
            Instant createdAt) {
        this(
                runId,
                tenantId,
                seq,
                type,
                1,
                null,
                null,
                seq > 1 ? seq - 1 : null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                payload,
                redacted,
                createdAt);
    }

    public AiRunEvent {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        if (runId == null || runId.isBlank() || seq <= 0 || type == null)
            throw new IllegalArgumentException("run event identity is required");
        if (schemaVersion <= 0) schemaVersion = 1;
        if (parentEventSeq != null && (parentEventSeq <= 0 || parentEventSeq >= seq)) {
            throw new IllegalArgumentException("parent event sequence must precede the event");
        }
        payload = payload == null ? "{}" : payload;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
