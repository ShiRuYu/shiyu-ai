package com.shiyu.ai.memory.implementation.domain.magma.model;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.model.*;

import java.util.Map;
import java.util.Objects;

/**
 * {@code MemoryEntity} 封装平台模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param entityType entityType 属性，表示该记录组件承载的数据。
 * @param externalRef externalRef 属性，表示该记录组件承载的数据。
 * @param displayName displayName 属性，表示该记录组件承载的数据。
 * @param normalizedName normalizedName 属性，表示该记录组件承载的数据。
 * @param attributes attributes 属性，表示该记录组件承载的数据。
 * @param active active 属性，表示该记录组件承载的数据。
 */
public record MemoryEntity(
        String id,
        TenantId tenantId,
        String entityType,
        String externalRef,
        String displayName,
        String normalizedName,
        Map<String, Object> attributes,
        boolean active) {
    public MemoryEntity {
        tenantId = Objects.requireNonNull(tenantId, "tenantId is required");
        if (id == null || id.isBlank()) throw new IllegalArgumentException("entity id is required");
        if (entityType == null || entityType.isBlank())
            throw new IllegalArgumentException("entityType is required");
        if (externalRef == null || externalRef.isBlank())
            throw new IllegalArgumentException("externalRef is required");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
