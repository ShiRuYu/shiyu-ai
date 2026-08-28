package com.shiyu.ai.memory.magma;

import com.shiyu.ai.kernel.context.TenantId;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record MemoryRetrievalTrace(String id, TenantId tenantId, String namespace, String queryText,
                                   List<String> anchorEventIds, Map<GraphType, Double> graphWeights,
                                   List<List<String>> relationPaths, List<String> filteredEventIds,
                                   List<String> resultEventIds, Instant createdAt) {
    public MemoryRetrievalTrace(String id, TenantId tenantId, String namespace, String queryText,
                                List<String> anchorEventIds, Instant createdAt) {
        this(id, tenantId, namespace, queryText, anchorEventIds, Map.of(), List.of(), List.of(),
                anchorEventIds, createdAt);
    }

    public MemoryRetrievalTrace {
        tenantId = Objects.requireNonNull(tenantId, "tenantId is required");
        anchorEventIds = anchorEventIds == null ? List.of() : List.copyOf(anchorEventIds);
        graphWeights = graphWeights == null ? Map.of() : Map.copyOf(graphWeights);
        relationPaths = relationPaths == null ? List.of() : relationPaths.stream().map(path -> path == null ? List.<String>of() : List.copyOf(path)).toList();
        filteredEventIds = filteredEventIds == null ? List.of() : List.copyOf(filteredEventIds);
        resultEventIds = resultEventIds == null ? anchorEventIds : List.copyOf(resultEventIds);
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
