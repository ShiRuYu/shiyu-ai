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
     * 原子认领一个仍处于暂停状态且检查点指针未变化的执行，以防多个实例同时续跑。
     *
     * @param tenantId 执行所属租户
     * @param executionId 执行标识
     * @param expectedCheckpointId 读取执行记录时的检查点标识，可为 null
     * @return 本次调用成功将状态从 PAUSED 改为 RUNNING 时返回 true
     */
    boolean claimPausedForResume(
            TenantId tenantId, String executionId, String expectedCheckpointId);

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
     * 查询当前租户中指定用户的执行，独立于会话 ID。
     *
     * @param tenantId 执行所属租户
     * @param userId 执行所属用户
     * @param limit 最大返回条数
     * @return 按开始时间倒序排列的执行
     */
    List<AgentExecutionBO> selectByUserId(TenantId tenantId, long userId, int limit);

    /**
     * 查询 智能体 Execution 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param agentId 用于定位agent的标识。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<AgentExecutionBO> selectByAgentId(TenantId tenantId, String agentId, int limit);

    /**
     * 查询租户内指定用户的 Agent 执行记录，并对结果应用数量限制。
     *
     * @param tenantId 记录所属租户
     * @param agentId 待查询执行记录的 Agent 标识
     * @param userId 执行记录所属用户标识
     * @param limit 最多返回的记录数
     * @return 按仓储顺序排列的匹配执行记录
     */
    List<AgentExecutionBO> selectByAgentIdAndUserId(
            TenantId tenantId, String agentId, long userId, int limit);
}
