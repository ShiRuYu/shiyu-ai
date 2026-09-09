package com.shiyu.ai.knowledge.implementation.web.response;

import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;

import java.util.List;

/**
 * 知识图谱邻域响应
 */
public record KnowledgeGraphResponse(
        KnowledgeResponse node,
        List<KnowledgeResponse> parentNodes,
        List<KnowledgeResponse> childNodes,
        List<KnowledgeResponse> relatedNodes
) {
}

