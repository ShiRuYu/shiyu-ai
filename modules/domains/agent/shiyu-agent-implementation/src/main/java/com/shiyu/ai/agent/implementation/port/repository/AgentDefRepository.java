package com.shiyu.ai.agent.implementation.port.repository;

import com.shiyu.ai.kernel.context.TenantId;

/**
 * AgentDefRepository 仓储接口，负责访问和持久化智能体领域聚合数据。
 */
public interface AgentDefRepository {
    /**
     * 统计符合条件的数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 操作影响的记录数或状态码。
     */
    long countByTenantId(TenantId tenantId);
}
