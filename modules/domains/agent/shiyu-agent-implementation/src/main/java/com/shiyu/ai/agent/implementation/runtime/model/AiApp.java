package com.shiyu.ai.agent.implementation.runtime.model;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * {@code AiApp} 封装智能体模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param ownerUserId 所属用户标识，表示该记录组件承载的数据。
 * @param name 名称，表示该记录组件承载的数据。
 * @param description 描述，表示该记录组件承载的数据。
 * @param status 状态，表示该记录组件承载的数据。
 * @param publishedVersionId publishedVersionId 属性，表示该记录组件承载的数据。
 * @param createdAt 创建时间，表示该记录组件承载的数据。
 * @param updatedAt 更新时间，表示该记录组件承载的数据。
 */
public record AiApp(
        String id,
        TenantId tenantId,
        UserId ownerUserId,
        String name,
        String description,
        String status,
        String publishedVersionId,
        Instant createdAt,
        Instant updatedAt) {
    public AiApp {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("app id is required");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(ownerUserId, "ownerUserId must not be null");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("app name is required");
        status = status == null || status.isBlank() ? "ACTIVE" : status;
        createdAt = createdAt == null ? Instant.now() : createdAt;
        updatedAt = updatedAt == null ? createdAt : updatedAt;
    }
}
