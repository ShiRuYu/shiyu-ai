package com.shiyu.ai.model.implementation.infrastructure.gateway;

import java.util.List;

/**
 * {@code ModelRoutePolicy} 封装模型模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param name 名称，表示该记录组件承载的数据。
 * @param orderedModels orderedModels 属性，表示该记录组件承载的数据。
 * @param timeoutMs timeoutMs 属性，表示该记录组件承载的数据。
 * @param fallbackOnError fallbackOnError 属性，表示该记录组件承载的数据。
 * @param maxTokens 最大令牌数，表示该记录组件承载的数据。
 */
public record ModelRoutePolicy(
        String id,
        long tenantId,
        String name,
        List<String> orderedModels,
        int timeoutMs,
        boolean fallbackOnError,
        long maxTokens) {
    public ModelRoutePolicy {
        if (id == null || id.isBlank() || tenantId <= 0 || name == null || name.isBlank())
            throw new IllegalArgumentException("route identity is required");
        orderedModels =
                orderedModels == null
                        ? List.of()
                        : orderedModels.stream()
                                .filter(model -> model != null && !model.isBlank())
                                .map(String::trim)
                                .toList();
        if (orderedModels.isEmpty())
            throw new IllegalArgumentException("at least one model is required");
        timeoutMs = timeoutMs <= 0 ? 30_000 : Math.min(timeoutMs, 300_000);
        maxTokens = maxTokens <= 0 ? 16_000 : Math.min(maxTokens, 128_000);
    }
}
