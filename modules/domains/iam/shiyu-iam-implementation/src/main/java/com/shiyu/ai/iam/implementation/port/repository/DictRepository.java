package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.DictBO;
import com.shiyu.ai.kernel.context.TenantId;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * DictRepository 仓储接口，负责访问和持久化身份与访问领域聚合数据。
 */
public interface DictRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param pageNo 方法参数。
     * @param pageSize 分页大小。
     *
     * @return 符合条件的结果集合。
     */
    Pair<Long, List<DictBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<DictBO> selectAll(TenantId tenantId);

    /**
     * 根据标识查询对应的数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    DictBO selectById(TenantId tenantId, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param dictType 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<DictBO> selectByDictType(TenantId tenantId, String dictType);

    /**
     * 创建并保存业务对象。
     *
     * @param dictBO 方法参数。
     *
     * @return 操作结果。
     */
    DictBO create(DictBO dictBO);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param dictBO 方法参数。
     *
     * @return 操作结果。
     */
    DictBO update(DictBO dictBO);

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
}
