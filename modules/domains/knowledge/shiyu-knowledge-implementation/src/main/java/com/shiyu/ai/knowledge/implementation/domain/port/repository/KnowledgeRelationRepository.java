package com.shiyu.ai.knowledge.implementation.domain.port.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeRelationBO;

import java.util.List;

/**
 * 负责 知识 关系 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface KnowledgeRelationRepository {
    /**
     * 查询 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param sourceId 用于定位source的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeRelationBO> findBySourceId(TenantId tenantId, Long spaceId, Long sourceId);

    /**
     * 查询 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param targetId 用于定位target的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeRelationBO> findByTargetId(TenantId tenantId, Long spaceId, Long targetId);

    /**
     * 查询 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param sourceId 用于定位source的标识。
     * @param type 用于完成本次业务处理的 type 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeRelationBO> findBySourceIdAndType(
            TenantId tenantId, Long spaceId, Long sourceId, String type);

    /**
     * 查询 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param targetId 用于定位target的标识。
     * @param type 用于完成本次业务处理的 type 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeRelationBO> findByTargetIdAndType(
            TenantId tenantId, Long spaceId, Long targetId, String type);

    /**
     * 创建或保存 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param bo 用于完成本次业务处理的 bo 参数。
     * @return 返回 知识 关系 相关操作生成的结果数据。
     */
    int insert(TenantId tenantId, KnowledgeRelationBO bo);

    /**
     * 删除或移除 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param sourceId 用于定位source的标识。
     * @param targetId 用于定位target的标识。
     * @param type 用于完成本次业务处理的 type 参数。
     * @return 返回 知识 关系 相关操作生成的结果数据。
     */
    int deleteBySourceAndTargetAndType(
            TenantId tenantId, Long spaceId, Long sourceId, Long targetId, String type);

    /**
     * 删除或移除 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     * @return 返回 知识 关系 相关操作生成的结果数据。
     */
    int deleteBySourceIdOrTargetId(TenantId tenantId, Long spaceId, Long knowledgeId);

    /**
     * 查询 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeRelationBO> findBySpace(TenantId tenantId, Long spaceId);

    /**
     * 执行 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param sourceId 用于定位source的标识。
     * @param targetId 用于定位target的标识。
     * @param type 用于完成本次业务处理的 type 参数。
     * @return 返回本次条件判断是否成立。
     */
    boolean exists(TenantId tenantId, Long spaceId, Long sourceId, Long targetId, String type);

    /**
     * 更新或设置 知识 关系 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     */
    void assignDefaultSpace(TenantId tenantId, Long spaceId);
}
