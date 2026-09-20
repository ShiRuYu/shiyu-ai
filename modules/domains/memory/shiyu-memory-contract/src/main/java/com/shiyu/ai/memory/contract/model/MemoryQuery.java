package com.shiyu.ai.memory.contract.model;

import com.shiyu.ai.kernel.context.TenantId;

import java.time.Instant;
import java.util.Set;

/**
 * 封装 记忆 相关的不可变数据及其字段约束。
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
     * 执行 记忆 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @param subjectType 用于完成本次业务处理的 subjectType 参数。
     * @param subjectId 用于定位subject的标识。
     * @param text 用于完成本次业务处理的 text 参数。
     * @param graphTypes 用于完成本次业务处理的 graphTypes 参数。
     * @param from 用于完成本次业务处理的 from 参数。
     * @param to 用于完成本次业务处理的 to 参数。
     * @param maxDepth 用于完成本次业务处理的 maxDepth 参数。
     * @param maxNodes 用于完成本次业务处理的 maxNodes 参数。
     * @param maxTokens 用于完成本次业务处理的 maxTokens 参数。
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
