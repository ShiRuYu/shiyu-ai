package com.shiyu.ai.knowledge.implementation.domain.port.provider;

import java.util.List;

/**
 * RerankProvider 边界接口，负责向外部组件提供知识领域相关能力。
 */
public interface RerankProvider {
    /**
     * 执行 {@code profile} 定义的接口操作。
     *
     * @return 操作结果。
     */
    String profile();

    /**
     * 执行 {@code rerank} 定义的接口操作。
     *
     * @param query 方法参数。
     * @param candidates 方法参数。
     * @param topK 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<Integer> rerank(String query, List<String> candidates, int topK);
}
