package com.shiyu.ai.knowledge.implementation.domain.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeBO;

import java.util.List;

/**
 * KnowledgeRepository 仓储接口，负责访问和持久化知识领域聚合数据。
 */
public interface KnowledgeRepository {
    /**
     * 根据标识查询对应的数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    KnowledgeBO findById(TenantId tenantId, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param code 方法参数。
     *
     * @return 操作结果。
     */
    KnowledgeBO findByCode(TenantId tenantId, String code);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeBO> findAll(TenantId tenantId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param keyword 方法参数。
     * @param topK 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeBO> searchByName(TenantId tenantId, String keyword, int topK);

    /**
     * 执行 {@code page} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param offset 方法参数。
     * @param limit 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeBO> page(TenantId tenantId, int offset, int limit);

    /**
     * 执行 {@code page} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param offset 方法参数。
     * @param limit 方法参数。
     * @param category 方法参数。
     * @param keyword 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeBO> page(
            TenantId tenantId, int offset, int limit, String category, String keyword);

    /**
     * 统计符合条件的数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 操作影响的记录数或状态码。
     */
    long count(TenantId tenantId);

    /**
     * 统计符合条件的数据。
     *
     * @param tenantId 租户标识。
     * @param category 方法参数。
     * @param keyword 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    long count(TenantId tenantId, String category, String keyword);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param bo 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int insert(TenantId tenantId, KnowledgeBO bo);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param tenantId 租户标识。
     * @param bo 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int update(TenantId tenantId, KnowledgeBO bo);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 操作影响的记录数或状态码。
     */
    int deleteById(TenantId tenantId, Long id);

    /**
     * 判断当前条件是否满足。
     *
     * @param tenantId 租户标识。
     * @param code 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean existsByCode(TenantId tenantId, String code);

    /**
     * 判断当前条件是否满足。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param code 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean existsBySpaceAndCode(TenantId tenantId, Long spaceId, String code);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeBO> findBySpace(TenantId tenantId, Long spaceId);

    /**
     * 执行 {@code pageBySpace} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param pageNum 页码。
     * @param pageSize 分页大小。
     * @param keyword 方法参数。
     * @param category 方法参数。
     *
     * @return 操作结果。
     */
    PageData<KnowledgeBO> pageBySpace(
            TenantId tenantId,
            Long spaceId,
            int pageNum,
            int pageSize,
            String keyword,
            String category);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     * @param spaceId 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int deleteByIdAndSpace(TenantId tenantId, Long id, Long spaceId);

    /**
     * 执行 {@code assignDefaultSpace} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     */
    void assignDefaultSpace(TenantId tenantId, Long spaceId);
}
