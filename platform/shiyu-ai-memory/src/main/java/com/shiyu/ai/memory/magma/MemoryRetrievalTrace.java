package com.shiyu.ai.memory.magma;

import java.time.Instant;
import java.util.List;

public record MemoryRetrievalTrace(String id, long tenantId, String namespace, String queryText,
                                   List<String> anchorEventIds, Instant createdAt) {
    public MemoryRetrievalTrace { anchorEventIds = anchorEventIds == null ? List.of() : List.copyOf(anchorEventIds); }
}
