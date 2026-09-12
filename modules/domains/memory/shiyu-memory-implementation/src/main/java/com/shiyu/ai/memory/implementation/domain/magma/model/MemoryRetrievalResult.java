package com.shiyu.ai.memory.implementation.domain.magma.model;

import com.shiyu.ai.memory.contract.model.*;

import java.util.List;

/**
 * 封装记忆检索路径及其追踪标识。
 * @param paths paths 属性，表示该记录组件承载的数据。
 * @param traceId traceId 属性，表示该记录组件承载的数据。
 */
public record MemoryRetrievalResult(List<MemoryPath> paths, String traceId) {
    public MemoryRetrievalResult {
        paths = paths == null ? List.of() : List.copyOf(paths);
    }
}
