package com.shiyu.ai.knowledge.implementation.domain.port.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocRelationBO;

import java.util.List;

/**
 * 负责 知识 Doc 关系 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface KnowledgeDocRelationRepository {
    /**
     * 创建或保存 知识 Doc 关系 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param relations 用于完成本次业务处理的 relations 参数。
     */
    void insertBatch(TenantId tenantId, List<KnowledgeDocRelationBO> relations);

    /**
     * 删除或移除 知识 Doc 关系 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param id 用于定位目标业务对象的标识。
     */
    void deleteByKnowledgeId(TenantId tenantId, Long spaceId, Long id);

    /**
     * 查询 知识 Doc 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeDocRelationBO> selectByDocId(TenantId tenantId, Long spaceId, Long id);

    /**
     * 查询 知识 Doc 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeDocRelationBO> selectByKnowledgeId(TenantId tenantId, Long spaceId, Long id);

    /**
     * 删除或移除 知识 Doc 关系 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param id 用于定位目标业务对象的标识。
     */
    void deleteByDocId(TenantId tenantId, Long spaceId, Long id);

    /**
     * 更新或设置 知识 Doc 关系 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     */
    void assignDefaultSpace(TenantId tenantId, Long spaceId);
}
