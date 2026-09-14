package com.shiyu.ai.agent.implementation.port.repository;

import com.shiyu.ai.agent.implementation.domain.model.AgentCheckpointBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * AgentCheckpointRepository 仓储接口，负责访问和持久化智能体领域聚合数据。
 */
public interface AgentCheckpointRepository {
    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param checkpoint 方法参数。
     */
    void insert(TenantId tenantId, AgentCheckpointBO checkpoint);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param checkpointId 方法参数。
     *
     * @return 操作结果。
     */
    AgentCheckpointBO selectByCheckpointId(TenantId tenantId, String checkpointId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param executionId 方法参数。
     *
     * @return 操作结果。
     */
    AgentCheckpointBO selectLatestByExecutionId(TenantId tenantId, String executionId);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param checkpointId 方法参数。
     */
    void deleteByCheckpointId(TenantId tenantId, String checkpointId);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param executionId 方法参数。
     */
    void deleteByExecutionId(TenantId tenantId, String executionId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param executionId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<AgentCheckpointBO> listByExecutionId(TenantId tenantId, String executionId);
}
