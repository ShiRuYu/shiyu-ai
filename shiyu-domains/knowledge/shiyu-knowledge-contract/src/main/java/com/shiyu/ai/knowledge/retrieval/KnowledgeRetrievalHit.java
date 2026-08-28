package com.shiyu.ai.knowledge.retrieval;

public record KnowledgeRetrievalHit(
        Long spaceId,
        Long knowledgeId,
        Long documentId,
        Long documentVersionId,
        Long chunkId,
        String title,
        String content,
        String highlight,
        Integer pageNumber,
        String sectionPath,
        double bm25Score,
        double vectorScore,
        double rrfScore,
        double rerankScore
) {
}
