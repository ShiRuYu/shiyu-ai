package com.shiyu.ai.memory.magma;
import java.util.List;
public interface MemoryRelationContributor<T> { List<MemoryEdge> contribute(T source); }
