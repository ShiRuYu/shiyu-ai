package com.shiyu.ai.evaluation;

import java.time.Instant;
import java.util.Map;

public record EvalCase(String id, String datasetId, long tenantId, String input, String expected, Map<String, Object> metadata, Instant createdAt) {
    public EvalCase {
        if (id == null || id.isBlank() || datasetId == null || datasetId.isBlank() || tenantId <= 0) throw new IllegalArgumentException("evaluation case identity is required");
        if (input == null || input.isBlank()) throw new IllegalArgumentException("evaluation input is required");
        expected = expected == null ? "" : expected;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
