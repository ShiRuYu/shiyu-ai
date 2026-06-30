package com.shiyu.ai.knowledge.dto;

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
