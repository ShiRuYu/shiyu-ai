package com.shiyu.ai.memory.implementation.domain.magma;

import com.shiyu.ai.memory.contract.model.*;

import java.util.List;

/** Query payload with the durable trace identifier used for explanation/replay. */
public record MemoryRetrievalResult(List<MemoryPath> paths, String traceId) {
    public MemoryRetrievalResult {
        paths = paths == null ? List.of() : List.copyOf(paths);
    }
}



