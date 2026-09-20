package com.shiyu.ai.knowledge.contract.model;

import java.util.List;

/**
 * 封装 知识 相关的不可变数据及其字段约束。
 */
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
        List<KnowledgeDocumentDTO> documents) {}
