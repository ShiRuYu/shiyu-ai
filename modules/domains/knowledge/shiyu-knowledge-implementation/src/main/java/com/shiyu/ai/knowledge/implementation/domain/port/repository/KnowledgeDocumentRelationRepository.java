package com.shiyu.ai.knowledge.implementation.domain.port.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentRelationBO;

import java.util.List;

/**
 * KnowledgeDocumentRelationRepository 仓储接口，负责访问和持久化知识领域聚合数据。
 */
public interface KnowledgeDocumentRelationRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param documentId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeDocumentRelationBO> selectBySource(
            TenantId tenantId, Long spaceId, Long documentId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param documentId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeDocumentRelationBO> selectByTarget(
            TenantId tenantId, Long spaceId, Long documentId);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param sourceId 方法参数。
     * @param relations 方法参数。
     */
    void replace(
            TenantId tenantId,
            Long spaceId,
            Long sourceId,
            List<KnowledgeDocumentRelationBO> relations);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param documentId 方法参数。
     */
    void deleteByDocument(TenantId tenantId, Long documentId);
}
