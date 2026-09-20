package com.shiyu.ai.agent.implementation.port.repository;

import com.shiyu.ai.agent.implementation.domain.model.AgentDefBO;
import com.shiyu.ai.agent.implementation.domain.model.AgentVersionBO;
import com.shiyu.ai.kernel.context.TenantId;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 负责 智能体 Admin 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface AgentAdminRepository {
    /**
     * 查询 智能体 Admin 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param pageNo 分页页码，从 1 开始。
     * @param pageSize 每页返回的数据数量。
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @param status 用于完成本次业务处理的 status 参数。
     * @return 返回总数及当前页数据，左值为总数，右值为数据列表。
     */
    Pair<Long, List<AgentDefBO>> selectPage(
            TenantId tenantId, Number pageNo, Number pageSize, String name, Integer status);

    /**
     * 查询 智能体 Admin 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 智能体 Admin 相关操作生成的结果数据。
     */
    AgentDefBO selectById(TenantId tenantId, Long id);

    /**
     * 查询 智能体 Admin 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param agentId 用于定位agent的标识。
     * @return 返回 智能体 Admin 相关操作生成的结果数据。
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
     * 创建或保存 智能体 Admin 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param agentDefBO 用于完成本次业务处理的 agentDefBO 参数。
     * @return 返回 智能体 Admin 相关操作生成的结果数据。
     */
    AgentDefBO create(TenantId tenantId, AgentDefBO agentDefBO);

    /**
     * 更新或设置 智能体 Admin 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param agentDefBO 用于完成本次业务处理的 agentDefBO 参数。
     * @return 返回 智能体 Admin 相关操作生成的结果数据。
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
     * 删除或移除 智能体 Admin 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param agentId 用于定位agent的标识。
     */
    void deleteByAgentId(TenantId tenantId, String agentId);

    /**
     * 查询 智能体 Admin 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param agentId 用于定位agent的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<AgentVersionBO> selectVersionsByAgentId(TenantId tenantId, String agentId);

    /**
     * 查询 智能体 Admin 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param versionId 用于定位version的标识。
     * @return 返回 智能体 Admin 相关操作生成的结果数据。
     */
    AgentVersionBO selectVersionById(TenantId tenantId, Long versionId);

    /**
     * 查询 智能体 Admin 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param agentId 用于定位agent的标识。
     * @param versionNumber 用于完成本次业务处理的 versionNumber 参数。
     * @return 返回 智能体 Admin 相关操作生成的结果数据。
     */
    AgentVersionBO selectVersionByAgentIdAndNumber(
            TenantId tenantId, String agentId, String versionNumber);

    /**
     * 创建或保存 智能体 Admin 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param versionBO 用于完成本次业务处理的 versionBO 参数。
     * @return 返回 智能体 Admin 相关操作生成的结果数据。
     */
    AgentVersionBO createVersion(TenantId tenantId, AgentVersionBO versionBO);

    /**
     * 更新或设置 智能体 Admin 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param versionBO 用于完成本次业务处理的 versionBO 参数。
     * @return 返回 智能体 Admin 相关操作生成的结果数据。
     */
    AgentVersionBO updateVersion(TenantId tenantId, AgentVersionBO versionBO);

    /**
     * 删除或移除 智能体 Admin 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param versionId 用于定位version的标识。
     */
    void deleteVersionById(TenantId tenantId, Long versionId);
}
