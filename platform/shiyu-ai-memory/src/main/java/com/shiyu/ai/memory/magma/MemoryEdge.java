package com.shiyu.ai.memory.magma;

import java.time.Instant;

public record MemoryEdge(
        String id,
        long tenantId,
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
        Instant createdAt
) {
    public MemoryEdge {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("edge id is required");
        if (sourceNodeId == null || targetNodeId == null) throw new IllegalArgumentException("edge nodes are required");
        if (graphType == null || origin == null) throw new IllegalArgumentException("edge type is required");
        weight = clamp(weight);
        confidence = clamp(confidence);
    }

    private static double clamp(double value) {
        return Math.max(0.0d, Math.min(1.0d, value));
    }
}
