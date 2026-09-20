package com.shiyu.ai.knowledge.contract.api;

import com.shiyu.ai.knowledge.contract.model.KnowledgeRetrievalRequest;
import com.shiyu.ai.knowledge.contract.model.KnowledgeRetrievalResult;

/**
 * 提供 知识 Retrieval 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface KnowledgeRetrievalService {

    /**
     * 获取知识retrieval。
     *
     * @param request 请求对象。
     *
     * @return 处理结果。
     */
    KnowledgeRetrievalResult retrieve(KnowledgeRetrievalRequest request);
}
