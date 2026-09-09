package com.shiyu.ai.memory.implementation.domain.magma;

import com.shiyu.ai.memory.contract.model.*;
import java.util.List;
public interface MemoryRelationContributor<T> { List<MemoryEdge> contribute(T source); }



