package com.shiyu.ai.agent.port.repository;


public interface AgentDefRepository {
    long countByTenantId(Long tenantId);
}
