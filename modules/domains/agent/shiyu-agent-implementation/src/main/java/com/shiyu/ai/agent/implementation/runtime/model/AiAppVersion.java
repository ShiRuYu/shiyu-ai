package com.shiyu.ai.agent.implementation.runtime.model;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;

import java.time.Instant;
import java.util.Objects;

/**
 * {@code AiAppVersion} 封装智能体模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param appId appId 属性，表示该记录组件承载的数据。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param version version 属性，表示该记录组件承载的数据。
 * @param configJson configJson 属性，表示该记录组件承载的数据。
 * @param status 状态，表示该记录组件承载的数据。
 * @param createdAt 创建时间，表示该记录组件承载的数据。
 * @param publishedAt publishedAt 属性，表示该记录组件承载的数据。
 */
public record AiAppVersion(
        String id,
        String appId,
        TenantId tenantId,
        String version,
        String configJson,
        String status,
        Instant createdAt,
        Instant publishedAt) {
    public AiAppVersion {
        if (id == null || id.isBlank() || appId == null || appId.isBlank())
            throw new IllegalArgumentException("app version identity is required");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        if (version == null || version.isBlank())
            throw new IllegalArgumentException("version is required");
        configJson = configJson == null ? "{}" : configJson;
        status = status == null || status.isBlank() ? "DRAFT" : status;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public boolean published() {
        return "PUBLISHED".equals(status);
    }
}
