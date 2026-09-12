package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.TenantBO;
import com.shiyu.ai.kernel.context.TenantId;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * TenantRepository 仓储接口，负责访问和持久化身份与访问领域聚合数据。
 */
public interface TenantRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param pageNo 方法参数。
     * @param pageSize 分页大小。
     * @param name 对象名称。
     * @param code 方法参数。
     * @param status 对象状态。
     *
     * @return 符合条件的结果集合。
     */
    Pair<Long, List<TenantBO>> selectPage(
            TenantId tenantId,
            Number pageNo,
            Number pageSize,
            String name,
            String code,
            Integer status);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @return 符合条件的结果集合。
     */
    List<TenantBO> selectAll();

    /**
     * 根据标识查询对应的数据。
     *
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    TenantBO selectById(Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 操作影响的记录数或状态码。
     */
    Long selectRootTenantId(TenantId tenantId);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantBO 方法参数。
     * @param sourceTenantId 方法参数。
     *
     * @return 操作结果。
     */
    TenantBO insert(TenantBO tenantBO, TenantId sourceTenantId);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param tenantBO 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean update(TenantBO tenantBO);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param id 目标对象标识。
     *
     * @return 条件是否满足。
     */
    boolean deleteById(Long id);

    /**
     * 判断当前条件是否满足。
     *
     * @param code 方法参数。
     * @param excludeId 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean existsByCode(String code, Long excludeId);

    /**
     * 执行 {@code cascadeDelete} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     */
    void cascadeDelete(TenantId tenantId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param rootId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<Long> selectDescendantIds(TenantId rootId);
}
