package com.shiyu.ai.model.implementation.domain.port.repository;

import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.model.implementation.domain.model.AiModelBO;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * AiModelRepository 仓储接口，负责访问和持久化模型领域聚合数据。
 */
public interface AiModelRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param platformId 方法参数。
     * @param pageNo 方法参数。
     * @param pageSize 分页大小。
     *
     * @return 符合条件的结果集合。
     */
    Pair<Long, List<AiModelBO>> selectPage(
            TenantId tenantId, Long platformId, Number pageNo, Number pageSize);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param platformId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<AiModelBO> selectByPlatformId(TenantId tenantId, Long platformId);

    /**
     * 根据标识查询对应的数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    AiModelBO selectById(TenantId tenantId, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param platformId 方法参数。
     *
     * @return 操作结果。
     */
    AiModelBO selectDefaultByPlatformId(TenantId tenantId, Long platformId);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param bo 方法参数。
     *
     * @return 操作结果。
     */
    AiModelBO create(TenantId tenantId, AiModelBO bo);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param tenantId 租户标识。
     * @param bo 方法参数。
     *
     * @return 操作结果。
     */
    AiModelBO update(TenantId tenantId, AiModelBO bo);

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
     * @param platformId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<IdNameOptionVO> selectOptions(TenantId tenantId, Long platformId);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param platformId 方法参数。
     * @param excludeId 方法参数。
     */
    void clearDefaultExcept(TenantId tenantId, Long platformId, Long excludeId);
}
