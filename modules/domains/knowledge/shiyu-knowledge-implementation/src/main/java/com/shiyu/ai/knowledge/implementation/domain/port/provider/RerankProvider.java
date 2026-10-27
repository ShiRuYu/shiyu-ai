package com.shiyu.ai.knowledge.implementation.domain.port.provider;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 创建或提供 Rerank 相关的业务组件和运行时能力。
 */
public interface RerankProvider {
    /**
     * 执行 Rerank 相关业务数据，并返回处理结果。
     *
     * @return 返回 Rerank 相关操作生成的结果数据。
     */
    String profile();

    /**
     * 按查询相关性返回候选片段在原列表中的索引，供调用方重排检索结果。
     *
     * @param tenantId 本次重排请求所属租户
     * @param query 用于评价片段相关性的查询文本
     * @param candidates 待排序的候选片段文本列表
     * @param topK 期望保留的最高相关片段数量
     * @return 按相关性从高到低排列的候选索引
     */
    List<Integer> rerank(TenantId tenantId, String query, List<String> candidates, int topK);
}
