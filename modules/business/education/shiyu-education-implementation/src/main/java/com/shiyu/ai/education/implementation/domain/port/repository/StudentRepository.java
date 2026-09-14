package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.implementation.domain.model.StudentBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * StudentRepository 仓储接口，负责访问和持久化教育领域聚合数据。
 */
public interface StudentRepository {
    /**
     * 根据标识查询对应的数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    StudentBO selectById(TenantId tenantId, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param userId 用户标识。
     *
     * @return 操作结果。
     */
    StudentBO selectByUserId(TenantId tenantId, Long userId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param pageNum 页码。
     * @param pageSize 分页大小。
     *
     * @return 操作结果。
     */
    PageData<StudentBO> selectPage(TenantId tenantId, int pageNum, int pageSize);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<StudentBO> selectAll(TenantId tenantId);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param entity 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int insert(TenantId tenantId, StudentBO entity);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param tenantId 租户标识。
     * @param entity 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int update(TenantId tenantId, StudentBO entity);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 操作影响的记录数或状态码。
     */
    int deleteById(TenantId tenantId, Long id);
}
