package com.shiyu.ai.memory.magma;

import com.shiyu.ai.kernel.context.TenantId;

public interface MemoryConsolidationPort { void retry(TenantId tenantId, long jobId); }
