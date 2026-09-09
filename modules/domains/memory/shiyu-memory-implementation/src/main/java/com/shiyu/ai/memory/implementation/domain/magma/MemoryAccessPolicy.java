package com.shiyu.ai.memory.implementation.domain.magma;

import com.shiyu.ai.memory.contract.model.*;
import com.shiyu.ai.kernel.context.TenantId;

public interface MemoryAccessPolicy { boolean canRead(TenantId tenantId, String namespace, String subjectType, String subjectId, String sourceType, String sourceId); }



