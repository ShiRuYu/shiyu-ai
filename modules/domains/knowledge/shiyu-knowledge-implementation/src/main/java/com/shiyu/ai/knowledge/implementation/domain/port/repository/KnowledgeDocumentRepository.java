package com.shiyu.ai.knowledge.implementation.domain.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentBO;

import java.util.List;

/**
 * KnowledgeDocumentRepository 仓储接口，负责访问和持久化知识领域聚合数据。
 */
public interface KnowledgeDocumentRepository {
    /**
     * 根据标识查询对应的数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    KnowledgeDocumentBO selectById(TenantId tenantId, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeDocumentBO> selectAll(TenantId tenantId);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param bo 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int insert(TenantId tenantId, KnowledgeDocumentBO bo);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param tenantId 租户标识。
     * @param bo 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int update(TenantId tenantId, KnowledgeDocumentBO bo);

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
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param keyword 方法参数。
     * @param topK 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeDocumentBO> searchByKeyword(TenantId tenantId, String keyword, int topK);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param knowledgeId 知识点标识。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeDocumentBO> selectByKnowledgeId(TenantId tenantId, Long knowledgeId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param knowledgeId 知识点标识。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeDocumentBO> selectByKnowledgeId(
            TenantId tenantId, Long spaceId, Long knowledgeId);

    /**
     * 执行 {@code pageBySpace} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param pageNum 页码。
     * @param pageSize 分页大小。
     * @param keyword 方法参数。
     * @param lifecycleStatus 方法参数。
     * @param parseStatus 方法参数。
     *
     * @return 操作结果。
     */
    PageData<KnowledgeDocumentBO> pageBySpace(
            TenantId tenantId,
            Long spaceId,
            int pageNum,
            int pageSize,
            String keyword,
            String lifecycleStatus,
            String parseStatus);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param checksum 方法参数。
     *
     * @return 操作结果。
     */
    KnowledgeDocumentBO findBySpaceAndChecksum(TenantId tenantId, Long spaceId, String checksum);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeDocumentBO> findBySpace(TenantId tenantId, Long spaceId);

    /**
     * 执行 {@code assignDefaultSpace} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     */
    void assignDefaultSpace(TenantId tenantId, Long spaceId);
}
