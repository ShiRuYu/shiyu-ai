package com.shiyu.ai.agent.implementation.port.repository;

import com.shiyu.ai.agent.implementation.domain.model.AgentExecutionBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * AgentExecutionRepository 仓储接口，负责访问和持久化智能体领域聚合数据。
 */
public interface AgentExecutionRepository {
    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param bo 方法参数。
     */
    void insert(TenantId tenantId, AgentExecutionBO bo);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param tenantId 租户标识。
     * @param bo 方法参数。
     */
    void update(TenantId tenantId, AgentExecutionBO bo);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param executionId 方法参数。
     *
     * @return 操作结果。
     */
    AgentExecutionBO selectByExecutionId(TenantId tenantId, String executionId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param sessionId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<AgentExecutionBO> selectBySessionId(TenantId tenantId, String sessionId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param agentId 方法参数。
     * @param limit 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<AgentExecutionBO> selectByAgentId(TenantId tenantId, String agentId, int limit);
}
