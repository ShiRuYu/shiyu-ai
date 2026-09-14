package com.shiyu.ai.memory.contract.model;

import com.shiyu.ai.kernel.context.TenantId;

import java.time.Instant;
import java.util.Objects;

/**
 * {@code MemoryEdge} 封装模型模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param sourceNodeId sourceNodeId 属性，表示该记录组件承载的数据。
 * @param targetNodeId targetNodeId 属性，表示该记录组件承载的数据。
 * @param graphType graphType 属性，表示该记录组件承载的数据。
 * @param relationType relationType 属性，表示该记录组件承载的数据。
 * @param directed directed 属性，表示该记录组件承载的数据。
 * @param weight weight 属性，表示该记录组件承载的数据。
 * @param confidence confidence 属性，表示该记录组件承载的数据。
 * @param origin origin 属性，表示该记录组件承载的数据。
 * @param evidenceSource evidenceSource 属性，表示该记录组件承载的数据。
 * @param active active 属性，表示该记录组件承载的数据。
 * @param createdAt 创建时间，表示该记录组件承载的数据。
 */
public record MemoryEdge(
        String id,
        TenantId tenantId,
        String sourceNodeId,
        String targetNodeId,
        GraphType graphType,
        String relationType,
        boolean directed,
        double weight,
        double confidence,
        EdgeOrigin origin,
        String evidenceSource,
        boolean active,
        Instant createdAt) {
    public MemoryEdge {
        tenantId = Objects.requireNonNull(tenantId, "tenantId is required");
        if (id == null || id.isBlank()) throw new IllegalArgumentException("edge id is required");
        if (sourceNodeId == null || targetNodeId == null)
            throw new IllegalArgumentException("edge nodes are required");
        if (graphType == null || origin == null)
            throw new IllegalArgumentException("edge type is required");
        weight = clamp(weight);
        confidence = clamp(confidence);
    }

    private static double clamp(double value) {
        return Math.max(0.0d, Math.min(1.0d, value));
    }
}
