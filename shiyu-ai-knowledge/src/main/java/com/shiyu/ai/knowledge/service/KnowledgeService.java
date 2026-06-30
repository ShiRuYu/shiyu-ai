package com.shiyu.ai.knowledge.service;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.knowledge.dto.CreateKnowledgeRequest;
import com.shiyu.ai.knowledge.dto.KnowledgeGraphResponse;
import com.shiyu.ai.knowledge.dto.KnowledgePageQuery;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.dto.UpdateKnowledgeRequest;

public interface KnowledgeService {

    KnowledgeResponse getById(Long id);

    PageData<KnowledgeResponse> page(KnowledgePageQuery query);

    KnowledgeResponse create(CreateKnowledgeRequest request);

    void update(Long id, UpdateKnowledgeRequest request);

    void delete(Long id);

    KnowledgeGraphResponse getGraph(Long id);
}
