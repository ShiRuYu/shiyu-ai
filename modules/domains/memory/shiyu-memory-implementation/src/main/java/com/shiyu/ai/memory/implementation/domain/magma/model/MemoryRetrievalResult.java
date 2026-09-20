package com.shiyu.ai.memory.implementation.domain.magma.model;

import com.shiyu.ai.memory.contract.model.*;

import java.util.List;

/**
 * 封装 记忆 Retrieval 相关的不可变数据及其字段约束。
 */
public record MemoryRetrievalResult(List<MemoryPath> paths, String traceId) {
    public MemoryRetrievalResult {
        paths = paths == null ? List.of() : List.copyOf(paths);
    }
}
