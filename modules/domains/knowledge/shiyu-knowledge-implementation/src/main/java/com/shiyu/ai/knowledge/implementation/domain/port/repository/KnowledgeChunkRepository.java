package com.shiyu.ai.knowledge.implementation.domain.port.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeChunkBO;

import java.util.List;

/**
 * KnowledgeChunkRepository 仓储接口，负责访问和持久化知识领域聚合数据。
 */
public interface KnowledgeChunkRepository {
    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param bo 方法参数。
     */
    void insert(TenantId tenantId, KnowledgeChunkBO bo);

    /**
     * 根据标识查询对应的数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    KnowledgeChunkBO getById(TenantId tenantId, Long id);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param documentId 方法参数。
     */
    void deleteByDocumentId(TenantId tenantId, Long documentId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeChunkBO> findBySpace(TenantId tenantId, Long spaceId);

    /**
     * 执行 {@code assignDefaultSpace} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     */
    void assignDefaultSpace(TenantId tenantId, Long spaceId);
}
