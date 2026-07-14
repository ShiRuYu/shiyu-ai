package com.shiyu.ai.knowledge.dto;

import java.util.List;

/**
 * 关联文档 DTO（从 KnowledgeResponse 中解耦，避免依赖 DocumentKnowledgeService 内部类型）
 */
public record KnowledgeDocumentDTO(
        Long id,
        String title,
        String content,
        String docType,
        String source,
        List<Long> knowledgeIds
) {}
