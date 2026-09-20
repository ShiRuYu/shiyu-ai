package com.shiyu.ai.model.implementation.domain.port.repository;

import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.model.implementation.domain.model.AiPlatformBO;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 负责 AI 平台 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface AiPlatformRepository {
    /**
     * 查询 AI 平台 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param pageNo 分页页码，从 1 开始。
     * @param pageSize 每页返回的数据数量。
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @return 返回总数及当前页数据，左值为总数，右值为数据列表。
     */
    Pair<Long, List<AiPlatformBO>> selectPage(
            TenantId tenantId, Number pageNo, Number pageSize, String name, String code);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<AiPlatformBO> selectAllEnabled(TenantId tenantId);

    /**
     * 查询 AI 平台 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 AI 平台 相关操作生成的结果数据。
     */
    AiPlatformBO selectById(TenantId tenantId, Long id);

    /**
     * 查询 AI 平台 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @return 返回 AI 平台 相关操作生成的结果数据。
     */
    AiPlatformBO selectByCode(TenantId tenantId, String code);

    /**
     * 查询 AI 平台 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回 AI 平台 相关操作生成的结果数据。
     */
    AiPlatformBO selectDefault(TenantId tenantId);

    /**
     * 创建或保存 AI 平台 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param bo 用于完成本次业务处理的 bo 参数。
     * @return 返回 AI 平台 相关操作生成的结果数据。
     */
    AiPlatformBO create(TenantId tenantId, AiPlatformBO bo);

    /**
     * 更新或设置 AI 平台 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param bo 用于完成本次业务处理的 bo 参数。
     * @return 返回 AI 平台 相关操作生成的结果数据。
     */
    AiPlatformBO update(TenantId tenantId, AiPlatformBO bo);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     */
    void deleteById(TenantId tenantId, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<IdNameOptionVO> selectOptions(TenantId tenantId);

    /**
     * 删除或移除 AI 平台 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param excludeId 用于定位exclude的标识。
     */
    void clearDefaultExcept(TenantId tenantId, Long excludeId);
}
