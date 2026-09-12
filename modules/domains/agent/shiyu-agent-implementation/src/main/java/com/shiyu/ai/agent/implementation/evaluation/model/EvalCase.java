package com.shiyu.ai.agent.implementation.evaluation.model;

import java.time.Instant;
import java.util.Map;

/**
 * {@code EvalCase} 封装智能体模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param datasetId datasetId 属性，表示该记录组件承载的数据。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param input 输入，表示该记录组件承载的数据。
 * @param expected expected 属性，表示该记录组件承载的数据。
 * @param metadata 元数据，表示该记录组件承载的数据。
 * @param createdAt 创建时间，表示该记录组件承载的数据。
 */
public record EvalCase(
        String id,
        String datasetId,
        long tenantId,
        String input,
        String expected,
        Map<String, Object> metadata,
        Instant createdAt) {
    public EvalCase {
        if (id == null || id.isBlank() || datasetId == null || datasetId.isBlank() || tenantId <= 0)
            throw new IllegalArgumentException("evaluation case identity is required");
        if (input == null || input.isBlank())
            throw new IllegalArgumentException("evaluation input is required");
        expected = expected == null ? "" : expected;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
