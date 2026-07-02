package com.shiyu.ai.knowledge.service;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.knowledge.dto.CreateKnowledgeRequest;
import com.shiyu.ai.knowledge.dto.KnowledgeGraphResponse;
import com.shiyu.ai.knowledge.dto.KnowledgePageQuery;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.dto.UpdateKnowledgeRequest;

/**
 * Knowledge 接口
 */

public interface KnowledgeService {

    /**
     * Get By Id
     * @return 处理结果
     */
    KnowledgeResponse getById(Long id);

    /**
     * Page
     * @param KnowledgePageQuery KnowledgePageQuery
     * @return 处理结果
     */
    PageData<KnowledgeResponse> page(KnowledgePageQuery query);

    /**
     * Create
     * @param CreateKnowledgeRequest CreateKnowledgeRequest
     * @return 处理结果
     */
    KnowledgeResponse create(CreateKnowledgeRequest request);

    /**
     * Update
     * @param UpdateKnowledgeRequest UpdateKnowledgeRequest
     * @return 处理结果
     */
    void update(Long id, UpdateKnowledgeRequest request);

    /**
     * Delete
     * @return 处理结果
     */
    void delete(Long id);

    /**
     * Get Graph
     * @return 处理结果
     */
    KnowledgeGraphResponse getGraph(Long id);
}
