package com.shiyu.ai.agent.implementation.evaluation.model;

import java.time.Instant;

/**
 * {@code EvalDataset} 封装智能体模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param ownerUserId 所属用户标识，表示该记录组件承载的数据。
 * @param name 名称，表示该记录组件承载的数据。
 * @param description 描述，表示该记录组件承载的数据。
 * @param createdAt 创建时间，表示该记录组件承载的数据。
 */
public record EvalDataset(
        String id,
        long tenantId,
        long ownerUserId,
        String name,
        String description,
        Instant createdAt) {
    public EvalDataset {
        if (id == null || id.isBlank() || tenantId <= 0 || ownerUserId <= 0)
            throw new IllegalArgumentException("dataset identity is required");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("dataset name is required");
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
