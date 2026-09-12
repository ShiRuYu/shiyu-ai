package com.shiyu.ai.knowledge.implementation.infrastructure.index.service;

import com.shiyu.ai.knowledge.implementation.infrastructure.index.port.FullTextIndex;
import com.shiyu.ai.knowledge.implementation.infrastructure.index.port.VectorIndex;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * KnowledgeIndexService 服务接口，负责执行知识领域相关业务操作。
 */
public interface KnowledgeIndexService extends FullTextIndex, VectorIndex {

    /**
     * 执行 {@code rebuild} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    long rebuild(TenantId tenantId, Long spaceId);

    /**
     * 执行 {@code hybridSearch} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param query 方法参数。
     * @param topK 方法参数。
     * @param rerank 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    default List<HybridHit> hybridSearch(
            TenantId tenantId, Long spaceId, String query, int topK, boolean rerank) {
        return hybridSearch(tenantId, spaceId, query, "HYBRID", topK, 0D, rerank);
    }

    /**
     * 执行 {@code hybridSearch} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param query 方法参数。
     * @param mode 方法参数。
     * @param topK 方法参数。
     * @param threshold 方法参数。
     * @param rerank 方法参数。
     *
     * @return 符合条件的结果集合。
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
     * 执行混合检索。
     *
     * @param actor 调用方上下文。
     * @param spaceId spaceId 参数。
     * @param query query 参数。
     * @param mode mode 参数。
     * @param topK topK 参数。
     * @param threshold threshold 参数。
     * @param rerank rerank 参数。
     *
     * @return 结果列表。
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
     * {@code HybridHit} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param chunkId chunkId 属性，表示该记录组件承载的数据。
     * @param documentId documentId 属性，表示该记录组件承载的数据。
     * @param content 内容，表示该记录组件承载的数据。
     * @param highlight highlight 属性，表示该记录组件承载的数据。
     * @param bm25Score bm25Score 属性，表示该记录组件承载的数据。
     * @param vectorScore vectorScore 属性，表示该记录组件承载的数据。
     * @param rrfScore rrfScore 属性，表示该记录组件承载的数据。
     * @param rerankScore rerankScore 属性，表示该记录组件承载的数据。
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
