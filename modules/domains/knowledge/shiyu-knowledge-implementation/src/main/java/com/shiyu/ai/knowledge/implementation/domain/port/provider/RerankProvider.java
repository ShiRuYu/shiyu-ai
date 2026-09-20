package com.shiyu.ai.knowledge.implementation.domain.port.provider;

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
     * 执行 Rerank 相关业务数据，并返回处理结果。
     *
     * @param query 用于筛选目标数据的查询条件。
     * @param candidates 用于完成本次业务处理的 candidates 参数。
     * @param topK 用于完成本次业务处理的 topK 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Integer> rerank(String query, List<String> candidates, int topK);
}
