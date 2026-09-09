package com.shiyu.ai.agent.implementation.port.repository;

import com.shiyu.ai.kernel.context.TenantId;

public interface AgentDefRepository {
    long countByTenantId(TenantId tenantId);
}
