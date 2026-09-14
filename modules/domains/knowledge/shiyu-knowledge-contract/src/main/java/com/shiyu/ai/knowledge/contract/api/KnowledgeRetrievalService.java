package com.shiyu.ai.knowledge.contract.api;

import com.shiyu.ai.knowledge.contract.model.KnowledgeRetrievalRequest;
import com.shiyu.ai.knowledge.contract.model.KnowledgeRetrievalResult;

/**
 * KnowledgeRetrievalService 服务接口，负责执行知识领域相关业务操作。
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
