package com.shiyu.ai.knowledge.retrieval;

public record KnowledgeCitation(
        String citationId,
        Long spaceId,
        Long knowledgeId,
        Long documentId,
        Long documentVersionId,
        Long chunkId,
        String title,
        Integer pageNumber,
        String sectionPath,
        String excerpt
) {
}
