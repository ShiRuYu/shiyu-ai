package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.implementation.domain.model.SubjectBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * SubjectRepository 仓储接口，负责访问和持久化教育领域聚合数据。
 */
public interface SubjectRepository {
    /**
     * 根据标识查询对应的数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    SubjectBO selectById(TenantId tenantId, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param code 方法参数。
     *
     * @return 操作结果。
     */
    SubjectBO selectByCode(TenantId tenantId, String code);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param pageNum 页码。
     * @param pageSize 分页大小。
     *
     * @return 操作结果。
     */
    PageData<SubjectBO> selectPage(TenantId tenantId, int pageNum, int pageSize);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param gradeLevel 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<SubjectBO> selectByGradeLevel(TenantId tenantId, String gradeLevel);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<SubjectBO> selectAll(TenantId tenantId);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param entity 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int insert(TenantId tenantId, SubjectBO entity);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param tenantId 租户标识。
     * @param entity 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int update(TenantId tenantId, SubjectBO entity);

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
