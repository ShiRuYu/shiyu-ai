package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.education.implementation.domain.model.StudentBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 负责 学生 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface StudentRepository {
    /**
     * 查询 学生 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 学生 相关操作生成的结果数据。
     */
    StudentBO selectById(TenantId tenantId, Long id);

    /**
     * 查询 学生 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param userId 当前操作涉及的用户标识。
     * @return 返回 学生 相关操作生成的结果数据。
     */
    StudentBO selectByUserId(TenantId tenantId, Long userId);

    /**
     * 查询 学生 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @return 返回 学生 相关操作生成的结果数据。
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
     * 创建或保存 学生 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param entity 用于完成本次业务处理的 entity 参数。
     * @return 返回 学生 相关操作生成的结果数据。
     */
    int insert(TenantId tenantId, StudentBO entity);

    /**
     * 更新或设置 学生 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param entity 用于完成本次业务处理的 entity 参数。
     * @return 返回 学生 相关操作生成的结果数据。
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
