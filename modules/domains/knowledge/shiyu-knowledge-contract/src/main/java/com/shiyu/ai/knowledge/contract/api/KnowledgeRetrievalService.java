package com.shiyu.ai.knowledge.contract.api;

import com.shiyu.ai.knowledge.contract.model.KnowledgeRetrievalRequest;
import com.shiyu.ai.knowledge.contract.model.KnowledgeRetrievalResult;

public interface KnowledgeRetrievalService {

    KnowledgeRetrievalResult retrieve(KnowledgeRetrievalRequest request);
}
