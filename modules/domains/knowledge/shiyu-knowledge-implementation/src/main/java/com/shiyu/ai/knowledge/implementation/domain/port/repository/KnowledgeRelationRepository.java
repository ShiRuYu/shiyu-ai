package com.shiyu.ai.knowledge.implementation.domain.port.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeRelationBO;

import java.util.List;

/**
 * KnowledgeRelationRepository 仓储接口，负责访问和持久化知识领域聚合数据。
 */
public interface KnowledgeRelationRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param sourceId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeRelationBO> findBySourceId(TenantId tenantId, Long spaceId, Long sourceId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param targetId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeRelationBO> findByTargetId(TenantId tenantId, Long spaceId, Long targetId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param sourceId 方法参数。
     * @param type 对象类型。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeRelationBO> findBySourceIdAndType(
            TenantId tenantId, Long spaceId, Long sourceId, String type);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param targetId 方法参数。
     * @param type 对象类型。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeRelationBO> findByTargetIdAndType(
            TenantId tenantId, Long spaceId, Long targetId, String type);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param bo 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int insert(TenantId tenantId, KnowledgeRelationBO bo);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param sourceId 方法参数。
     * @param targetId 方法参数。
     * @param type 对象类型。
     *
     * @return 操作影响的记录数或状态码。
     */
    int deleteBySourceAndTargetAndType(
            TenantId tenantId, Long spaceId, Long sourceId, Long targetId, String type);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param knowledgeId 知识点标识。
     *
     * @return 操作影响的记录数或状态码。
     */
    int deleteBySourceIdOrTargetId(TenantId tenantId, Long spaceId, Long knowledgeId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeRelationBO> findBySpace(TenantId tenantId, Long spaceId);

    /**
     * 判断当前条件是否满足。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param sourceId 方法参数。
     * @param targetId 方法参数。
     * @param type 对象类型。
     *
     * @return 条件是否满足。
     */
    boolean exists(TenantId tenantId, Long spaceId, Long sourceId, Long targetId, String type);

    /**
     * 执行 {@code assignDefaultSpace} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     */
    void assignDefaultSpace(TenantId tenantId, Long spaceId);
}
