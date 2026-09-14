package com.shiyu.ai.memory.contract.model;

import com.shiyu.ai.kernel.context.TenantId;

import java.time.Instant;
import java.util.Set;

/**
 * {@code MemoryQuery} 封装模型模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param namespace 命名空间，表示该记录组件承载的数据。
 * @param subjectType subjectType 属性，表示该记录组件承载的数据。
 * @param subjectId subjectId 属性，表示该记录组件承载的数据。
 * @param text text 属性，表示该记录组件承载的数据。
 * @param graphTypes graphTypes 属性，表示该记录组件承载的数据。
 * @param from from 属性，表示该记录组件承载的数据。
 * @param to to 属性，表示该记录组件承载的数据。
 * @param maxDepth maxDepth 属性，表示该记录组件承载的数据。
 * @param maxNodes maxNodes 属性，表示该记录组件承载的数据。
 * @param maxTokens 最大令牌数，表示该记录组件承载的数据。
 * @param intent intent 属性，表示该记录组件承载的数据。
 */
public record MemoryQuery(
        TenantId tenantId,
        String namespace,
        String subjectType,
        String subjectId,
        String text,
        Set<GraphType> graphTypes,
        Instant from,
        Instant to,
        int maxDepth,
        int maxNodes,
        int maxTokens,
        MemoryQueryIntent intent) {
    /**
     * 处理memoryquery。
     *
     * @param tenantId 租户标识。
     * @param namespace namespace 参数。
     * @param subjectType subjectType 参数。
     * @param subjectId subjectId 参数。
     * @param text text 参数。
     * @param graphTypes graphTypes 参数。
     * @param from from 参数。
     * @param to to 参数。
     * @param maxDepth maxDepth 参数。
     * @param maxNodes maxNodes 参数。
     * @param maxTokens maxTokens 参数。
     *
     * @return 结果列表。
     */
    public MemoryQuery(
            TenantId tenantId,
            String namespace,
            String subjectType,
            String subjectId,
            String text,
            Set<GraphType> graphTypes,
            Instant from,
            Instant to,
            int maxDepth,
            int maxNodes,
            int maxTokens) {
        this(
                tenantId,
                namespace,
                subjectType,
                subjectId,
                text,
                graphTypes,
                from,
                to,
                maxDepth,
                maxNodes,
                maxTokens,
                MemoryQueryIntent.infer(text));
    }

    public MemoryQuery {
        if (tenantId == null) throw new IllegalArgumentException("tenantId is required");
        if (text == null || text.isBlank())
            throw new IllegalArgumentException("query text is required");
        if (namespace == null || namespace.isBlank())
            throw new IllegalArgumentException("namespace is required");
        graphTypes =
                graphTypes == null || graphTypes.isEmpty()
                        ? Set.of(
                                GraphType.SEMANTIC,
                                GraphType.TEMPORAL,
                                GraphType.ENTITY,
                                GraphType.CAUSAL)
                        : Set.copyOf(graphTypes);
        maxDepth = maxDepth <= 0 ? 2 : Math.min(maxDepth, 8);
        maxNodes = maxNodes <= 0 ? 20 : Math.min(maxNodes, 200);
        maxTokens = maxTokens <= 0 ? 2000 : Math.min(maxTokens, 16000);
        intent = intent == null ? MemoryQueryIntent.infer(text) : intent;
    }
}
