package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.implementation.domain.model.SubjectBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 负责 学科 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface SubjectRepository {
    /**
     * 查询 学科 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 学科 相关操作生成的结果数据。
     */
    SubjectBO selectById(TenantId tenantId, Long id);

    /**
     * 查询 学科 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @return 返回 学科 相关操作生成的结果数据。
     */
    SubjectBO selectByCode(TenantId tenantId, String code);

    /**
     * 查询 学科 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @return 返回 学科 相关操作生成的结果数据。
     */
    PageData<SubjectBO> selectPage(TenantId tenantId, int pageNum, int pageSize);

    /**
     * 查询 学科 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param gradeLevel 用于完成本次业务处理的 gradeLevel 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 创建或保存 学科 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param entity 用于完成本次业务处理的 entity 参数。
     * @return 返回 学科 相关操作生成的结果数据。
     */
    int insert(TenantId tenantId, SubjectBO entity);

    /**
     * 更新或设置 学科 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param entity 用于完成本次业务处理的 entity 参数。
     * @return 返回 学科 相关操作生成的结果数据。
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
