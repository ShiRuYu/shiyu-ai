package com.shiyu.ai.evaluation;

import java.time.Instant;
import java.util.List;

public record EvalRun(String id, String datasetId, long tenantId, long ownerUserId, String appVersionId, EvalMetric metric, String status, double passRate, List<EvalResult> results, Instant createdAt, Instant completedAt) {
    public EvalRun {
        if (id == null || id.isBlank() || datasetId == null || datasetId.isBlank() || tenantId <= 0 || ownerUserId <= 0) throw new IllegalArgumentException("evaluation run identity is required");
        metric = metric == null ? EvalMetric.EXACT_MATCH : metric;
        status = status == null ? "CREATED" : status;
        results = results == null ? List.of() : List.copyOf(results);
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
