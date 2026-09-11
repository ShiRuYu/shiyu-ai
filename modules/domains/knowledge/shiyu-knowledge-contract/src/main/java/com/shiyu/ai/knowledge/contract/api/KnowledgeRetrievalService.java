package com.shiyu.ai.knowledge.contract.api;

import com.shiyu.ai.knowledge.contract.model.KnowledgeRetrievalRequest;
import com.shiyu.ai.knowledge.contract.model.KnowledgeRetrievalResult;

/** Cross-domain retrieval contract for ranked knowledge context. */
public interface KnowledgeRetrievalService {

    /** Retrieves tenant-authorized knowledge for the supplied query. */
    KnowledgeRetrievalResult retrieve(KnowledgeRetrievalRequest request);
}
