package com.shiyu.ai.agent.implementation.evaluation.model;

import java.time.Instant;
import java.util.List;

/**
 * {@code EvalRun} 封装智能体模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param datasetId datasetId 属性，表示该记录组件承载的数据。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param ownerUserId 所属用户标识，表示该记录组件承载的数据。
 * @param appVersionId appVersionId 属性，表示该记录组件承载的数据。
 * @param metric metric 属性，表示该记录组件承载的数据。
 * @param status 状态，表示该记录组件承载的数据。
 * @param passRate passRate 属性，表示该记录组件承载的数据。
 * @param results results 属性，表示该记录组件承载的数据。
 * @param createdAt 创建时间，表示该记录组件承载的数据。
 * @param completedAt completedAt 属性，表示该记录组件承载的数据。
 */
public record EvalRun(
        String id,
        String datasetId,
        long tenantId,
        long ownerUserId,
        String appVersionId,
        EvalMetric metric,
        String status,
        double passRate,
        List<EvalResult> results,
        Instant createdAt,
        Instant completedAt) {
    public EvalRun {
        if (id == null
                || id.isBlank()
                || datasetId == null
                || datasetId.isBlank()
                || tenantId <= 0
                || ownerUserId <= 0)
            throw new IllegalArgumentException("evaluation run identity is required");
        metric = metric == null ? EvalMetric.EXACT_MATCH : metric;
        status = status == null ? "CREATED" : status;
        results = results == null ? List.of() : List.copyOf(results);
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
