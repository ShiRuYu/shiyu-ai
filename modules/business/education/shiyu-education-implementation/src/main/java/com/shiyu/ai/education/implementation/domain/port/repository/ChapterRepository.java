package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.ChapterBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * ChapterRepository 仓储接口，负责访问和持久化教育领域聚合数据。
 */
public interface ChapterRepository {
    /**
     * 根据标识查询对应的数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    ChapterBO selectById(TenantId tenantId, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param textbookId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<ChapterBO> selectByTextbookId(TenantId tenantId, Long textbookId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<ChapterBO> selectAll(TenantId tenantId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param textbookId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<ChapterBO> selectRootChapters(TenantId tenantId, Long textbookId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param parentId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<ChapterBO> selectByParentId(TenantId tenantId, Long parentId);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param entity 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int insert(TenantId tenantId, ChapterBO entity);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param tenantId 租户标识。
     * @param entity 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int update(TenantId tenantId, ChapterBO entity);

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
