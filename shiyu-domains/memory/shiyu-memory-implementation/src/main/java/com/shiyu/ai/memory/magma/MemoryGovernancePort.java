package com.shiyu.ai.memory.magma;

import com.shiyu.ai.kernel.context.TenantId;

public interface MemoryGovernancePort {
    void confirm(TenantId tenantId, String eventId);
    void revoke(TenantId tenantId, String eventId);
    MemoryEvent supersede(TenantId tenantId, String oldEventId, IngestMemoryCommand replacement);
}
