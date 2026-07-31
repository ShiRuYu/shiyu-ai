package com.shiyu.ai.knowledge.dto;

import java.util.List;

/**
 * 知识点关联文档摘要。
 */
public record KnowledgeDocumentDTO(
        Long id,
        String title,
        String content,
        String docType,
        String source,
        List<Long> knowledgeIds
) {}
