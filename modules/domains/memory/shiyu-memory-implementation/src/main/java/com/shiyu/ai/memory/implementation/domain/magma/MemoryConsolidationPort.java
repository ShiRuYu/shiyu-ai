package com.shiyu.ai.memory.implementation.domain.magma;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.model.*;

public interface MemoryConsolidationPort {
    void retry(TenantId tenantId, long jobId);
}
