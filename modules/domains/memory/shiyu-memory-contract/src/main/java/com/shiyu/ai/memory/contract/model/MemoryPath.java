package com.shiyu.ai.memory.contract.model;

import java.util.List;

/**
 * 封装 记忆 Path 相关的不可变数据及其字段约束。
 */
public record MemoryPath(MemoryEvent event, double score, List<MemoryEdge> edges) {
    public MemoryPath {
        edges = edges == null ? List.of() : List.copyOf(edges);
    }
}
