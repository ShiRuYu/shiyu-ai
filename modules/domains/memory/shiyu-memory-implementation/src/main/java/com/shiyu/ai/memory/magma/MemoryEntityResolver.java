package com.shiyu.ai.memory.magma;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.Optional;
public interface MemoryEntityResolver { Optional<MemoryEntity> resolve(TenantId tenantId, String entityType, String externalRef); }
