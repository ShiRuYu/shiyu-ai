package com.shiyu.ai.memory.implementation.domain.magma;

import com.shiyu.ai.memory.contract.model.*;

import java.util.Map;

public interface MemoryRetrievalPolicyProvider {
    Map<GraphType, Double> weights(MemoryQuery query);
}
