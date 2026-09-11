package com.shiyu.ai.memory.implementation.domain.magma;

import com.shiyu.ai.memory.contract.model.*;

public interface MemoryIndexPort {
    void rebuild(String namespace);
}
