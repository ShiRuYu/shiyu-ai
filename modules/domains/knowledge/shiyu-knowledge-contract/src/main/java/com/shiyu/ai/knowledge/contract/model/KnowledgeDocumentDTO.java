package com.shiyu.ai.knowledge.contract.model;

import java.util.List;

/** Stable document summary exposed to other bounded contexts. */
public record KnowledgeDocumentDTO(
        Long id,
        String title,
        String content,
        String docType,
        String source,
        List<Long> knowledgeIds) {}
