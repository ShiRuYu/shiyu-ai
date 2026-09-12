package com.shiyu.ai.memory.implementation.domain.magma.model;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.model.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@code MemoryRetrievalTrace} 封装平台模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param namespace 命名空间，表示该记录组件承载的数据。
 * @param queryText queryText 属性，表示该记录组件承载的数据。
 * @param anchorEventIds anchorEventIds 属性，表示该记录组件承载的数据。
 * @param graphWeights graphWeights 属性，表示该记录组件承载的数据。
 * @param relationPaths relationPaths 属性，表示该记录组件承载的数据。
 * @param filteredEventIds filteredEventIds 属性，表示该记录组件承载的数据。
 * @param resultEventIds resultEventIds 属性，表示该记录组件承载的数据。
 * @param createdAt 创建时间，表示该记录组件承载的数据。
 */
public record MemoryRetrievalTrace(
        String id,
        TenantId tenantId,
        String namespace,
        String queryText,
        List<String> anchorEventIds,
        Map<GraphType, Double> graphWeights,
        List<List<String>> relationPaths,
        List<String> filteredEventIds,
        List<String> resultEventIds,
        Instant createdAt) {
    public MemoryRetrievalTrace(
            String id,
            TenantId tenantId,
            String namespace,
            String queryText,
            List<String> anchorEventIds,
            Instant createdAt) {
        this(
                id,
                tenantId,
                namespace,
                queryText,
                anchorEventIds,
                Map.of(),
                List.of(),
                List.of(),
                anchorEventIds,
                createdAt);
    }

    public MemoryRetrievalTrace {
        tenantId = Objects.requireNonNull(tenantId, "tenantId is required");
        anchorEventIds = anchorEventIds == null ? List.of() : List.copyOf(anchorEventIds);
        graphWeights = graphWeights == null ? Map.of() : Map.copyOf(graphWeights);
        relationPaths =
                relationPaths == null
                        ? List.of()
                        : relationPaths.stream()
                                .map(path -> path == null ? List.<String>of() : List.copyOf(path))
                                .toList();
        filteredEventIds = filteredEventIds == null ? List.of() : List.copyOf(filteredEventIds);
        resultEventIds = resultEventIds == null ? anchorEventIds : List.copyOf(resultEventIds);
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
