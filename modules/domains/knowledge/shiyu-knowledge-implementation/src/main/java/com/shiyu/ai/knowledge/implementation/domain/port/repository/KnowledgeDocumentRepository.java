package com.shiyu.ai.knowledge.implementation.domain.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentBO;

import java.util.List;

/**
 * 负责 知识 文档 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface KnowledgeDocumentRepository {
    /**
     * 查询 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 创建或保存 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param bo 用于完成本次业务处理的 bo 参数。
     * @return 返回 知识 文档 相关操作生成的结果数据。
     */
    int insert(TenantId tenantId, KnowledgeDocumentBO bo);

    /**
     * 更新或设置 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param bo 用于完成本次业务处理的 bo 参数。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 查询 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param keyword 用于完成本次业务处理的 keyword 参数。
     * @param topK 用于完成本次业务处理的 topK 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 查询 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeDocumentBO> selectByKnowledgeId(
            TenantId tenantId, Long spaceId, Long knowledgeId);

    /**
     * 查询 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param keyword 用于完成本次业务处理的 keyword 参数。
     * @param lifecycleStatus 用于完成本次业务处理的 lifecycleStatus 参数。
     * @param parseStatus 用于完成本次业务处理的 parseStatus 参数。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 查询 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param checksum 用于完成本次业务处理的 checksum 参数。
     * @return 返回 知识 文档 相关操作生成的结果数据。
     */
    KnowledgeDocumentBO findBySpaceAndChecksum(TenantId tenantId, Long spaceId, String checksum);

    /**
     * 查询 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeDocumentBO> findBySpace(TenantId tenantId, Long spaceId);

    /**
     * 更新或设置 知识 文档 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     */
    void assignDefaultSpace(TenantId tenantId, Long spaceId);
}
