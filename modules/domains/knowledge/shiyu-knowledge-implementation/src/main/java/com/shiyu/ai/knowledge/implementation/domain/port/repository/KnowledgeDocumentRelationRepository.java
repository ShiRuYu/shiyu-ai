package com.shiyu.ai.knowledge.implementation.domain.port.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentRelationBO;

import java.util.List;

/**
 * 负责 知识 文档 关系 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface KnowledgeDocumentRelationRepository {
    /**
     * 查询 知识 文档 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param documentId 用于定位document的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeDocumentRelationBO> selectBySource(
            TenantId tenantId, Long spaceId, Long documentId);

    /**
     * 查询 知识 文档 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param documentId 用于定位document的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeDocumentRelationBO> selectByTarget(
            TenantId tenantId, Long spaceId, Long documentId);

    /**
     * 执行 知识 文档 关系 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param sourceId 用于定位source的标识。
     * @param relations 用于完成本次业务处理的 relations 参数。
     */
    void replace(
            TenantId tenantId,
            Long spaceId,
            Long sourceId,
            List<KnowledgeDocumentRelationBO> relations);

    /**
     * 删除或移除 知识 文档 关系 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param documentId 用于定位document的标识。
     */
    void deleteByDocument(TenantId tenantId, Long documentId);
}
