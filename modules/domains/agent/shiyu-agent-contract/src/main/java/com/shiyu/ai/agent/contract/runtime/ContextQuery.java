package com.shiyu.ai.agent.contract.runtime;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import java.util.Map;

/**
 * 描述 Agent 上下文检索所需的租户、用户、空间和查询条件。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param ownerUserId 所属用户标识，表示该记录组件承载的数据。
 * @param namespace 命名空间，表示该记录组件承载的数据。
 * @param text text 属性，表示该记录组件承载的数据。
 * @param topK topK 属性，表示该记录组件承载的数据。
 * @param filters filters 属性，表示该记录组件承载的数据。
 */
public record ContextQuery(
        TenantId tenantId,
        UserId ownerUserId,
        String namespace,
        String text,
        int topK,
        Map<String, String> filters) {
    public ContextQuery {
        if (tenantId == null || ownerUserId == null)
            throw new IllegalArgumentException("tenant and owner are required");
        if (text == null || text.isBlank())
            throw new IllegalArgumentException("context query is required");
        topK = topK <= 0 ? 5 : Math.min(topK, 50);
        filters = filters == null ? Map.of() : Map.copyOf(filters);
    }
}
