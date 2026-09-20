package com.shiyu.ai.agent.implementation.port.repository;

import com.shiyu.ai.agent.implementation.domain.model.AgentExecutionBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 负责 智能体 Execution 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface AgentExecutionRepository {
    /**
     * 创建或保存 智能体 Execution 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param bo 用于完成本次业务处理的 bo 参数。
     */
    void insert(TenantId tenantId, AgentExecutionBO bo);

    /**
     * 更新或设置 智能体 Execution 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param bo 用于完成本次业务处理的 bo 参数。
     */
    void update(TenantId tenantId, AgentExecutionBO bo);

    /**
     * 查询 智能体 Execution 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param executionId 用于定位execution的标识。
     * @return 返回 智能体 Execution 相关操作生成的结果数据。
     */
    AgentExecutionBO selectByExecutionId(TenantId tenantId, String executionId);

    /**
     * 查询 智能体 Execution 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param sessionId 用于定位session的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<AgentExecutionBO> selectBySessionId(TenantId tenantId, String sessionId);

    /**
     * 查询 智能体 Execution 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param agentId 用于定位agent的标识。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<AgentExecutionBO> selectByAgentId(TenantId tenantId, String agentId, int limit);
}
