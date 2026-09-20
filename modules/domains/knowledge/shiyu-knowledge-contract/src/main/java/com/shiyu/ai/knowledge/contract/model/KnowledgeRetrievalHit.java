package com.shiyu.ai.knowledge.contract.model;

/**
 * 封装 知识 Retrieval Hit 相关的不可变数据及其字段约束。
 */
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
        double rerankScore) {}
