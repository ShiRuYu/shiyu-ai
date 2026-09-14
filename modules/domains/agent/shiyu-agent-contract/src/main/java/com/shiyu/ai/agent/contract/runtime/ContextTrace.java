package com.shiyu.ai.agent.contract.runtime;

import com.shiyu.ai.kernel.context.TenantId;

import java.time.Instant;
import java.util.List;

/**
 * {@code ContextTrace} 封装智能体模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param query query 属性，表示该记录组件承载的数据。
 * @param itemIds itemIds 属性，表示该记录组件承载的数据。
 * @param policy policy 属性，表示该记录组件承载的数据。
 * @param createdAt 创建时间，表示该记录组件承载的数据。
 */
public record ContextTrace(
        String id,
        TenantId tenantId,
        String query,
        List<String> itemIds,
        String policy,
        Instant createdAt) {
    public ContextTrace {
        if (tenantId == null) throw new IllegalArgumentException("tenant is required");
        itemIds = itemIds == null ? List.of() : List.copyOf(itemIds);
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
