package com.shiyu.ai.memory.contract.model;

import com.shiyu.ai.kernel.context.TenantId;

import java.time.Instant;
import java.util.Map;

/**
 * {@code IngestMemoryCommand} 封装模型模块中不可变的结构化数据，并作为相关操作之间的值对象。
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
 * @param confirmationPolicy confirmationPolicy 属性，表示该记录组件承载的数据。
 */
public record IngestMemoryCommand(
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
        ConfirmationPolicy confirmationPolicy) {
    public IngestMemoryCommand {
        if (tenantId == null) throw new IllegalArgumentException("tenantId is required");
        if (namespace == null || namespace.isBlank())
            throw new IllegalArgumentException("namespace is required");
        if (subjectType == null || subjectType.isBlank())
            throw new IllegalArgumentException("subjectType is required");
        if (subjectId == null || subjectId.isBlank())
            throw new IllegalArgumentException("subjectId is required");
        if (eventType == null || eventType.isBlank())
            throw new IllegalArgumentException("eventType is required");
        if (content == null || content.isBlank())
            throw new IllegalArgumentException("content is required");
        if (occurredAt == null) occurredAt = Instant.now();
        if (confirmationPolicy == null) confirmationPolicy = ConfirmationPolicy.REQUIRED;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
