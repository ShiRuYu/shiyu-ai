package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.KnowledgeTextbookBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 负责 知识 教材 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface KnowledgeTextbookRepository {
    /**
     * 创建或保存 知识 教材 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param kt 用于完成本次业务处理的 kt 参数。
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
     * 删除或移除 知识 教材 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param cid 用于定位c的标识。
     */
    void deleteByChapterId(TenantId tenantId, Long cid);

    /**
     * 删除或移除 知识 教材 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param kid 用于定位k的标识。
     * @param cid 用于定位c的标识。
     */
    void deleteByKnowledgeIdAndChapterId(TenantId tenantId, Long kid, Long cid);

    /**
     * 查询 知识 教材 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param cid 用于定位c的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeTextbookBO> selectByChapterId(TenantId tenantId, Long cid);

    /**
     * 查询 知识 教材 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param kid 用于定位k的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeTextbookBO> selectByKnowledgeId(TenantId tenantId, Long kid);

    /**
     * 查询 知识 教材 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param tid 用于定位t的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeTextbookBO> selectByTextbookId(TenantId tenantId, Long tid);
}
