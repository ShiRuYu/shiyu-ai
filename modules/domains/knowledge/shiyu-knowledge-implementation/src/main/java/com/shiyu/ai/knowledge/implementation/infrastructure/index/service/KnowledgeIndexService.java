package com.shiyu.ai.knowledge.implementation.infrastructure.index.service;

import com.shiyu.ai.knowledge.implementation.infrastructure.index.port.FullTextIndex;
import com.shiyu.ai.knowledge.implementation.infrastructure.index.port.VectorIndex;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 提供 知识 索引 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface KnowledgeIndexService extends FullTextIndex, VectorIndex {

    /**
     * 执行 知识 索引 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @return 返回 知识 索引 相关操作生成的结果数据。
     */
    long rebuild(TenantId tenantId, Long spaceId);

    /**
     * 执行 知识 索引 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param query 用于筛选目标数据的查询条件。
     * @param topK 用于完成本次业务处理的 topK 参数。
     * @param rerank 用于完成本次业务处理的 rerank 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    default List<HybridHit> hybridSearch(
            TenantId tenantId, Long spaceId, String query, int topK, boolean rerank) {
        return hybridSearch(tenantId, spaceId, query, "HYBRID", topK, 0D, rerank);
    }

    /**
     * 执行 知识 索引 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param query 用于筛选目标数据的查询条件。
     * @param mode 用于完成本次业务处理的 mode 参数。
     * @param topK 用于完成本次业务处理的 topK 参数。
     * @param threshold 用于完成本次业务处理的 threshold 参数。
     * @param rerank 用于完成本次业务处理的 rerank 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<HybridHit> hybridSearch(
            TenantId tenantId,
            Long spaceId,
            String query,
            String mode,
            int topK,
            double threshold,
            boolean rerank);

    /**
     * 执行 知识 索引 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param spaceId 用于定位space的标识。
     * @param query 用于筛选目标数据的查询条件。
     * @param mode 用于完成本次业务处理的 mode 参数。
     * @param topK 用于完成本次业务处理的 topK 参数。
     * @param threshold 用于完成本次业务处理的 threshold 参数。
     * @param rerank 用于完成本次业务处理的 rerank 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    default List<HybridHit> hybridSearch(
            ActorContext actor,
            Long spaceId,
            String query,
            String mode,
            int topK,
            double threshold,
            boolean rerank) {
        if (actor == null) {
            throw new IllegalArgumentException("actor is required");
        }
        return hybridSearch(actor.tenantId(), spaceId, query, mode, topK, threshold, rerank);
    }

    /**
     * 封装 Hybrid Hit 相关的不可变数据及其字段约束。
     */
    record HybridHit(
            Long chunkId,
            Long documentId,
            String content,
            String highlight,
            double bm25Score,
            double vectorScore,
            double rrfScore,
            double rerankScore) {}
}
