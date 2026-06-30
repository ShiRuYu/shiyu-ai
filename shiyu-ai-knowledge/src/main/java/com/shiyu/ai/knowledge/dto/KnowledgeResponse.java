package com.shiyu.ai.knowledge.dto;

import com.shiyu.ai.knowledge.service.DocumentKnowledgeService;

import java.util.List;

public record KnowledgeResponse(
        Long id,
        String code,
        String name,
        String description,
        Integer difficulty,
        String category,
        String tags,
        List<Long> parentIds,
        List<Long> childIds,
        List<DocumentKnowledgeService.KnowledgeDocumentVO> documents
) {
}
