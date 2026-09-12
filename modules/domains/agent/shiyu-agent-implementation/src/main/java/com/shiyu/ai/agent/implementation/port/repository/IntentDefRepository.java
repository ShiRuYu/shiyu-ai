package com.shiyu.ai.agent.implementation.port.repository;

import com.shiyu.ai.agent.implementation.domain.model.IntentDefBO;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.kernel.context.TenantId;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * IntentDefRepository 仓储接口，负责访问和持久化智能体领域聚合数据。
 */
public interface IntentDefRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param pageNo 方法参数。
     * @param pageSize 分页大小。
     * @param agentId 方法参数。
     * @param name 对象名称。
     * @param code 方法参数。
     * @param category 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    Pair<Long, List<IntentDefBO>> selectPage(
            TenantId tenantId,
            Number pageNo,
            Number pageSize,
            String agentId,
            String name,
            String code,
            String category);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param agentId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<IntentDefBO> selectByAgentId(TenantId tenantId, String agentId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param agentId 方法参数。
     * @param category 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<IntentDefBO> selectByCategory(TenantId tenantId, String agentId, String category);

    /**
     * 根据标识查询对应的数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    IntentDefBO selectById(TenantId tenantId, Long id);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param bo 方法参数。
     *
     * @return 操作结果。
     */
    IntentDefBO create(TenantId tenantId, IntentDefBO bo);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param tenantId 租户标识。
     * @param bo 方法参数。
     *
     * @return 操作结果。
     */
    IntentDefBO update(TenantId tenantId, IntentDefBO bo);

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
     * @param ids 目标对象标识集合。
     */
    void deleteByIds(TenantId tenantId, List<Long> ids);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<IdNameOptionVO> selectAllOptions(TenantId tenantId);
}
