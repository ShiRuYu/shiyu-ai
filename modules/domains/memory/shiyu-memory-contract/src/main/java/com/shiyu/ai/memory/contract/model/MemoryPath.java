package com.shiyu.ai.memory.contract.model;

import java.util.List;

public record MemoryPath(MemoryEvent event, double score, List<MemoryEdge> edges) {
    public MemoryPath {
        edges = edges == null ? List.of() : List.copyOf(edges);
    }
}

