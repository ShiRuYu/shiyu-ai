package com.shiyu.ai.memory.implementation.domain.magma;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.model.*;

import java.util.List;

public interface MemorySemanticIndex {
    void upsert(MemoryEvent event);

    List<MemoryPath> search(MemoryQuery query, int limit);

    void delete(String eventId);

    void rebuild(TenantId tenantId, String namespace);
}
