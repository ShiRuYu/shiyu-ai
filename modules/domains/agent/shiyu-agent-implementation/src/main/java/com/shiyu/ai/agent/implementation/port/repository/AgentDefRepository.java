package com.shiyu.ai.agent.implementation.port.repository;

import com.shiyu.ai.kernel.context.TenantId;

/**
 * 负责 智能体 Def 的持久化查询、保存和删除，并维护数据访问边界。
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
