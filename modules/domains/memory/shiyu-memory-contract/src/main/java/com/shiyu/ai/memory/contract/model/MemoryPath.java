package com.shiyu.ai.memory.contract.model;

import java.util.List;

/**
 * {@code MemoryPath} 封装模型模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param event event 属性，表示该记录组件承载的数据。
 * @param score 分数，表示该记录组件承载的数据。
 * @param edges edges 属性，表示该记录组件承载的数据。
 */
public record MemoryPath(MemoryEvent event, double score, List<MemoryEdge> edges) {
    public MemoryPath {
        edges = edges == null ? List.of() : List.copyOf(edges);
    }
}
