package com.shiyu.ai.agent.implementation.port.repository;

import com.shiyu.ai.agent.implementation.domain.model.IntentDefBO;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.kernel.context.TenantId;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 负责 Intent Def 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface IntentDefRepository {
    /**
     * 查询 Intent Def 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param pageNo 分页页码，从 1 开始。
     * @param pageSize 每页返回的数据数量。
     * @param agentId 用于定位agent的标识。
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param category 用于完成本次业务处理的 category 参数。
     * @return 返回总数及当前页数据，左值为总数，右值为数据列表。
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
     * 查询 Intent Def 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param agentId 用于定位agent的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<IntentDefBO> selectByAgentId(TenantId tenantId, String agentId);

    /**
     * 查询 Intent Def 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param agentId 用于定位agent的标识。
     * @param category 用于完成本次业务处理的 category 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<IntentDefBO> selectByCategory(TenantId tenantId, String agentId, String category);

    /**
     * 查询 Intent Def 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 Intent Def 相关操作生成的结果数据。
     */
    IntentDefBO selectById(TenantId tenantId, Long id);

    /**
     * 创建或保存 Intent Def 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param bo 用于完成本次业务处理的 bo 参数。
     * @return 返回 Intent Def 相关操作生成的结果数据。
     */
    IntentDefBO create(TenantId tenantId, IntentDefBO bo);

    /**
     * 更新或设置 Intent Def 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param bo 用于完成本次业务处理的 bo 参数。
     * @return 返回 Intent Def 相关操作生成的结果数据。
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
