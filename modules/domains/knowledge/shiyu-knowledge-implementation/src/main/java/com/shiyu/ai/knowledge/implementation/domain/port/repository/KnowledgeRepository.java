package com.shiyu.ai.knowledge.implementation.domain.port.repository;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeBO;

import java.util.List;

/**
 * 负责 知识 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface KnowledgeRepository {
    /**
     * 查询 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 相关操作生成的结果数据。
     */
    KnowledgeBO findById(TenantId tenantId, Long id);

    /**
     * 查询 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @return 返回 知识 相关操作生成的结果数据。
     */
    KnowledgeBO findByCode(TenantId tenantId, String code);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeBO> findAll(TenantId tenantId);

    /**
     * 查询 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param keyword 用于完成本次业务处理的 keyword 参数。
     * @param topK 用于完成本次业务处理的 topK 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeBO> searchByName(TenantId tenantId, String keyword, int topK);

    /**
     * 查询 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param offset 用于完成本次业务处理的 offset 参数。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeBO> page(TenantId tenantId, int offset, int limit);

    /**
     * 查询 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param offset 用于完成本次业务处理的 offset 参数。
     * @param limit 每页返回的数据数量。
     * @param category 用于完成本次业务处理的 category 参数。
     * @param keyword 用于完成本次业务处理的 keyword 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeBO> page(
            TenantId tenantId, int offset, int limit, String category, String keyword);

    /**
     * 统计符合条件的数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 操作影响的记录数或状态码。
     */
    long count(TenantId tenantId);

    /**
     * 执行 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param category 用于完成本次业务处理的 category 参数。
     * @param keyword 用于完成本次业务处理的 keyword 参数。
     * @return 返回 知识 相关操作生成的结果数据。
     */
    long count(TenantId tenantId, String category, String keyword);

    /**
     * 创建或保存 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param bo 用于完成本次业务处理的 bo 参数。
     * @return 返回 知识 相关操作生成的结果数据。
     */
    int insert(TenantId tenantId, KnowledgeBO bo);

    /**
     * 更新或设置 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param bo 用于完成本次业务处理的 bo 参数。
     * @return 返回 知识 相关操作生成的结果数据。
     */
    int update(TenantId tenantId, KnowledgeBO bo);

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
     * 执行 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @return 返回本次条件判断是否成立。
     */
    boolean existsByCode(TenantId tenantId, String code);

    /**
     * 执行 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @return 返回本次条件判断是否成立。
     */
    boolean existsBySpaceAndCode(TenantId tenantId, Long spaceId, String code);

    /**
     * 查询 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeBO> findBySpace(TenantId tenantId, Long spaceId);

    /**
     * 查询 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param keyword 用于完成本次业务处理的 keyword 参数。
     * @param category 用于完成本次业务处理的 category 参数。
     * @return 返回 知识 相关操作生成的结果数据。
     */
    PageData<KnowledgeBO> pageBySpace(
            TenantId tenantId,
            Long spaceId,
            int pageNum,
            int pageSize,
            String keyword,
            String category);

    /**
     * 删除或移除 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @param spaceId 用于定位space的标识。
     * @return 返回 知识 相关操作生成的结果数据。
     */
    int deleteByIdAndSpace(TenantId tenantId, Long id, Long spaceId);

    /**
     * 更新或设置 知识 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     */
    void assignDefaultSpace(TenantId tenantId, Long spaceId);
}
