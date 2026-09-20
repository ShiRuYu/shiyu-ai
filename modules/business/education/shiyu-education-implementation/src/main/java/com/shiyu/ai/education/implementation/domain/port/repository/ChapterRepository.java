package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.ChapterBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 负责 章节 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface ChapterRepository {
    /**
     * 查询 章节 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 章节 相关操作生成的结果数据。
     */
    ChapterBO selectById(TenantId tenantId, Long id);

    /**
     * 查询 章节 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param textbookId 用于定位textbook的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 查询 章节 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param textbookId 用于定位textbook的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<ChapterBO> selectRootChapters(TenantId tenantId, Long textbookId);

    /**
     * 查询 章节 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param parentId 用于定位parent的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<ChapterBO> selectByParentId(TenantId tenantId, Long parentId);

    /**
     * 创建或保存 章节 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param entity 用于完成本次业务处理的 entity 参数。
     * @return 返回 章节 相关操作生成的结果数据。
     */
    int insert(TenantId tenantId, ChapterBO entity);

    /**
     * 更新或设置 章节 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param entity 用于完成本次业务处理的 entity 参数。
     * @return 返回 章节 相关操作生成的结果数据。
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
