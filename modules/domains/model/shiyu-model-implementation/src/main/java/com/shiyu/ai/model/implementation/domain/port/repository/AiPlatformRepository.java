package com.shiyu.ai.model.implementation.domain.port.repository;

import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.model.implementation.domain.model.AiPlatformBO;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * AiPlatformRepository 仓储接口，负责访问和持久化模型领域聚合数据。
 */
public interface AiPlatformRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param pageNo 方法参数。
     * @param pageSize 分页大小。
     * @param name 对象名称。
     * @param code 方法参数。
     *
     * @return 符合条件的结果集合。
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
     * 根据标识查询对应的数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    AiPlatformBO selectById(TenantId tenantId, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param code 方法参数。
     *
     * @return 操作结果。
     */
    AiPlatformBO selectByCode(TenantId tenantId, String code);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 操作结果。
     */
    AiPlatformBO selectDefault(TenantId tenantId);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param bo 方法参数。
     *
     * @return 操作结果。
     */
    AiPlatformBO create(TenantId tenantId, AiPlatformBO bo);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param tenantId 租户标识。
     * @param bo 方法参数。
     *
     * @return 操作结果。
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
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param excludeId 方法参数。
     */
    void clearDefaultExcept(TenantId tenantId, Long excludeId);
}
