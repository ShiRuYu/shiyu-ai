package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.implementation.domain.model.ResourceBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 负责 资源 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface ResourceRepository {
    /**
     * 查询 资源 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 资源 相关操作生成的结果数据。
     */
    ResourceBO selectById(TenantId tenantId, Long id);

    /**
     * 查询 资源 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param subjectCode 用于完成本次业务处理的 subjectCode 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<ResourceBO> selectBySubjectCode(TenantId tenantId, String subjectCode);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param type 对象类型。
     *
     * @return 符合条件的结果集合。
     */
    List<ResourceBO> selectByType(TenantId tenantId, String type);

    /**
     * 查询 资源 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @return 返回 资源 相关操作生成的结果数据。
     */
    PageData<ResourceBO> selectPage(TenantId tenantId, int pageNum, int pageSize);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<ResourceBO> selectAll(TenantId tenantId);

    /**
     * 创建或保存 资源 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param entity 用于完成本次业务处理的 entity 参数。
     * @return 返回 资源 相关操作生成的结果数据。
     */
    int insert(TenantId tenantId, ResourceBO entity);

    /**
     * 更新或设置 资源 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param entity 用于完成本次业务处理的 entity 参数。
     * @return 返回 资源 相关操作生成的结果数据。
     */
    int update(TenantId tenantId, ResourceBO entity);

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
