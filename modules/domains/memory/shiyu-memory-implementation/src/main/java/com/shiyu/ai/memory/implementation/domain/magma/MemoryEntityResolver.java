package com.shiyu.ai.memory.implementation.domain.magma;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.model.*;

import java.util.Optional;

public interface MemoryEntityResolver {
    Optional<MemoryEntity> resolve(TenantId tenantId, String entityType, String externalRef);
}
