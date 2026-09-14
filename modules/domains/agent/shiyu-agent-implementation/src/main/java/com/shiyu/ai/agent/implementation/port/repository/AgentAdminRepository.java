package com.shiyu.ai.agent.implementation.port.repository;

import com.shiyu.ai.agent.implementation.domain.model.AgentDefBO;
import com.shiyu.ai.agent.implementation.domain.model.AgentVersionBO;
import com.shiyu.ai.kernel.context.TenantId;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * AgentAdminRepository 仓储接口，负责访问和持久化智能体领域聚合数据。
 */
public interface AgentAdminRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param pageNo 方法参数。
     * @param pageSize 分页大小。
     * @param name 对象名称。
     * @param status 对象状态。
     *
     * @return 符合条件的结果集合。
     */
    Pair<Long, List<AgentDefBO>> selectPage(
            TenantId tenantId, Number pageNo, Number pageSize, String name, Integer status);

    /**
     * 根据标识查询对应的数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    AgentDefBO selectById(TenantId tenantId, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param agentId 方法参数。
     *
     * @return 操作结果。
     */
    AgentDefBO selectByAgentId(TenantId tenantId, String agentId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<AgentDefBO> selectAllActive(TenantId tenantId);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param agentDefBO 方法参数。
     *
     * @return 操作结果。
     */
    AgentDefBO create(TenantId tenantId, AgentDefBO agentDefBO);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param tenantId 租户标识。
     * @param agentDefBO 方法参数。
     *
     * @return 操作结果。
     */
    AgentDefBO update(TenantId tenantId, AgentDefBO agentDefBO);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     */
    void deleteById(TenantId tenantId, Long id);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param agentId 方法参数。
     */
    void deleteByAgentId(TenantId tenantId, String agentId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param agentId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<AgentVersionBO> selectVersionsByAgentId(TenantId tenantId, String agentId);

    /**
     * 根据标识查询对应的数据。
     *
     * @param tenantId 租户标识。
     * @param versionId 方法参数。
     *
     * @return 操作结果。
     */
    AgentVersionBO selectVersionById(TenantId tenantId, Long versionId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param agentId 方法参数。
     * @param versionNumber 方法参数。
     *
     * @return 操作结果。
     */
    AgentVersionBO selectVersionByAgentIdAndNumber(
            TenantId tenantId, String agentId, String versionNumber);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param versionBO 方法参数。
     *
     * @return 操作结果。
     */
    AgentVersionBO createVersion(TenantId tenantId, AgentVersionBO versionBO);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param tenantId 租户标识。
     * @param versionBO 方法参数。
     *
     * @return 操作结果。
     */
    AgentVersionBO updateVersion(TenantId tenantId, AgentVersionBO versionBO);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param versionId 方法参数。
     */
    void deleteVersionById(TenantId tenantId, Long versionId);
}
