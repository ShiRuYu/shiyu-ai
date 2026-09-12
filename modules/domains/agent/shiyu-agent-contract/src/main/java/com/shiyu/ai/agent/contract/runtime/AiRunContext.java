package com.shiyu.ai.agent.contract.runtime;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.Map;

/**
 * {@code AiRunContext} 封装智能体模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param ownerUserId 所属用户标识，表示该记录组件承载的数据。
 * @param appId appId 属性，表示该记录组件承载的数据。
 * @param appVersionId appVersionId 属性，表示该记录组件承载的数据。
 * @param conversationId conversationId 属性，表示该记录组件承载的数据。
 * @param generationId generationId 属性，表示该记录组件承载的数据。
 * @param executionId executionId 属性，表示该记录组件承载的数据。
 * @param traceId traceId 属性，表示该记录组件承载的数据。
 * @param attributes attributes 属性，表示该记录组件承载的数据。
 */
public record AiRunContext(
        TenantId tenantId,
        long ownerUserId,
        String appId,
        String appVersionId,
        String conversationId,
        String generationId,
        String executionId,
        String traceId,
        Map<String, String> attributes) {
    public AiRunContext {
        if (tenantId == null || tenantId.value() <= 0 || ownerUserId <= 0) {
            throw new IllegalArgumentException("tenant and owner are required");
        }
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
