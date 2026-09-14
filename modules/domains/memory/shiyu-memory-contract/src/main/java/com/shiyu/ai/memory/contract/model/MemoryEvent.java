package com.shiyu.ai.memory.contract.model;

import com.shiyu.ai.kernel.context.TenantId;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * {@code MemoryEvent} 封装模型模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param namespace 命名空间，表示该记录组件承载的数据。
 * @param subjectType subjectType 属性，表示该记录组件承载的数据。
 * @param subjectId subjectId 属性，表示该记录组件承载的数据。
 * @param eventType eventType 属性，表示该记录组件承载的数据。
 * @param content 内容，表示该记录组件承载的数据。
 * @param occurredAt occurredAt 属性，表示该记录组件承载的数据。
 * @param sourceType sourceType 属性，表示该记录组件承载的数据。
 * @param sourceId sourceId 属性，表示该记录组件承载的数据。
 * @param attributes attributes 属性，表示该记录组件承载的数据。
 * @param confidence confidence 属性，表示该记录组件承载的数据。
 * @param importance importance 属性，表示该记录组件承载的数据。
 * @param status 状态，表示该记录组件承载的数据。
 * @param confirmationPolicy confirmationPolicy 属性，表示该记录组件承载的数据。
 * @param createdAt 创建时间，表示该记录组件承载的数据。
 * @param updatedAt 更新时间，表示该记录组件承载的数据。
 */
public record MemoryEvent(
        String id,
        TenantId tenantId,
        String namespace,
        String subjectType,
        String subjectId,
        String eventType,
        String content,
        Instant occurredAt,
        String sourceType,
        String sourceId,
        Map<String, Object> attributes,
        double confidence,
        double importance,
        MemoryEventStatus status,
        ConfirmationPolicy confirmationPolicy,
        Instant createdAt,
        Instant updatedAt) {
    public MemoryEvent {
        tenantId = Objects.requireNonNull(tenantId, "tenantId is required");
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("memory event id is required");
        if (namespace == null || namespace.isBlank())
            throw new IllegalArgumentException("namespace is required");
        if (eventType == null || eventType.isBlank())
            throw new IllegalArgumentException("eventType is required");
        if (content == null || content.isBlank())
            throw new IllegalArgumentException("content is required");
        if (status == null || confirmationPolicy == null)
            throw new IllegalArgumentException("event policy is required");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        confidence = clamp(confidence);
        importance = clamp(importance);
    }

    private static double clamp(double value) {
        return Math.max(0.0d, Math.min(1.0d, value));
    }
}
