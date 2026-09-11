package com.shiyu.ai.memory.contract.api;

import com.shiyu.ai.memory.contract.model.MemoryPath;
import com.shiyu.ai.memory.contract.model.MemoryQuery;

import java.util.List;

/** Read boundary for explainable memory paths within a tenant scope. */
public interface MemoryQueryPort {
    /** Retrieves matching paths ordered by the implementation's relevance policy. */
    List<MemoryPath> retrieve(MemoryQuery query);
}
