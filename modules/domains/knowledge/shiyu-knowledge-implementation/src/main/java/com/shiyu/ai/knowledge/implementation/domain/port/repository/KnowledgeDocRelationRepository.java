package com.shiyu.ai.knowledge.implementation.domain.port.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocRelationBO;

import java.util.List;

/**
 * KnowledgeDocRelationRepository 仓储接口，负责访问和持久化知识领域聚合数据。
 */
public interface KnowledgeDocRelationRepository {
    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param relations 方法参数。
     */
    void insertBatch(TenantId tenantId, List<KnowledgeDocRelationBO> relations);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param id 目标对象标识。
     */
    void deleteByKnowledgeId(TenantId tenantId, Long spaceId, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param id 目标对象标识。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeDocRelationBO> selectByDocId(TenantId tenantId, Long spaceId, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param id 目标对象标识。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeDocRelationBO> selectByKnowledgeId(TenantId tenantId, Long spaceId, Long id);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param id 目标对象标识。
     */
    void deleteByDocId(TenantId tenantId, Long spaceId, Long id);

    /**
     * 执行 {@code assignDefaultSpace} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     */
    void assignDefaultSpace(TenantId tenantId, Long spaceId);
}
