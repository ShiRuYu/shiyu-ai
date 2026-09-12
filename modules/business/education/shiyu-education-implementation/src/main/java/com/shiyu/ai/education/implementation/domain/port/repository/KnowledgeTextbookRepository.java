package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.KnowledgeTextbookBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * KnowledgeTextbookRepository 仓储接口，负责访问和持久化教育领域聚合数据。
 */
public interface KnowledgeTextbookRepository {
    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param kt 方法参数。
     */
    void insert(TenantId tenantId, KnowledgeTextbookBO kt);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     */
    void deleteById(TenantId tenantId, Long id);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param cid 方法参数。
     */
    void deleteByChapterId(TenantId tenantId, Long cid);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param kid 方法参数。
     * @param cid 方法参数。
     */
    void deleteByKnowledgeIdAndChapterId(TenantId tenantId, Long kid, Long cid);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param cid 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeTextbookBO> selectByChapterId(TenantId tenantId, Long cid);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param kid 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeTextbookBO> selectByKnowledgeId(TenantId tenantId, Long kid);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param tid 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeTextbookBO> selectByTextbookId(TenantId tenantId, Long tid);
}
