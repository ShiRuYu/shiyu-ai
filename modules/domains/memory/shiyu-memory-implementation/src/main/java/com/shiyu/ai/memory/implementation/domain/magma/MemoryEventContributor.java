package com.shiyu.ai.memory.implementation.domain.magma;

import com.shiyu.ai.memory.contract.model.*;
public interface MemoryEventContributor<T> { IngestMemoryCommand contribute(T source); }



