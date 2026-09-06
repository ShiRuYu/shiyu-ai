package com.shiyu.ai.memory.magma;
import java.util.Map;
public interface MemoryRetrievalPolicyProvider { Map<GraphType, Double> weights(MemoryQuery query); }
