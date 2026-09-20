package com.shiyu.ai.knowledge.contract.model;

/**
 * 封装 知识 Citation 相关的不可变数据及其字段约束。
 */
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
        String excerpt) {}
