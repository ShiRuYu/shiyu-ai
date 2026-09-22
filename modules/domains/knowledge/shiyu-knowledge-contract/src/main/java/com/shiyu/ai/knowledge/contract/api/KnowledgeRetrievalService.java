package com.shiyu.ai.knowledge.contract.api;

import com.shiyu.ai.knowledge.contract.model.KnowledgeRetrievalRequest;
import com.shiyu.ai.knowledge.contract.model.KnowledgeRetrievalResult;

/**
 * 提供知识检索服务，根据租户和知识空间范围检索相关内容并返回命中片段、引用及上下文。
 */
public interface KnowledgeRetrievalService {

    /**
     * 根据检索请求执行知识空间内的相关性检索。
     *
     * @param request 检索请求，包含租户、空间、查询文本、召回数量及可选重排条件。
     *
     * @return 检索结果，包含命中内容、来源引用、相关性分数及供模型使用的上下文。
     */
    KnowledgeRetrievalResult retrieve(KnowledgeRetrievalRequest request);
}
