package com.shiyu.ai.agent.contract.runtime;

import com.shiyu.ai.kernel.context.TenantId;

import java.time.Instant;
import java.util.List;

/**
 * 封装 Context Trace 相关的不可变数据及其字段约束。
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
