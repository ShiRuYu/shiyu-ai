package com.shiyu.ai.memory.magma;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record MemoryRetrievalTrace(String id, long tenantId, String namespace, String queryText,
                                   List<String> anchorEventIds, Map<GraphType, Double> graphWeights,
                                   List<List<String>> relationPaths, List<String> filteredEventIds,
                                   List<String> resultEventIds, Instant createdAt) {
    public MemoryRetrievalTrace(String id, long tenantId, String namespace, String queryText,
                                List<String> anchorEventIds, Instant createdAt) {
        this(id, tenantId, namespace, queryText, anchorEventIds, Map.of(), List.of(), List.of(),
                anchorEventIds, createdAt);
    }

    public MemoryRetrievalTrace {
        anchorEventIds = anchorEventIds == null ? List.of() : List.copyOf(anchorEventIds);
        graphWeights = graphWeights == null ? Map.of() : Map.copyOf(graphWeights);
        relationPaths = relationPaths == null ? List.of() : relationPaths.stream().map(path -> path == null ? List.<String>of() : List.copyOf(path)).toList();
        filteredEventIds = filteredEventIds == null ? List.of() : List.copyOf(filteredEventIds);
        resultEventIds = resultEventIds == null ? anchorEventIds : List.copyOf(resultEventIds);
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
