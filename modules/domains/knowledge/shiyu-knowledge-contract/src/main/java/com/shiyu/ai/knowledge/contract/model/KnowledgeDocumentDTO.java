package com.shiyu.ai.knowledge.contract.model;

import java.util.List;
import java.io.Serializable;

/**
 * 封装 知识 文档 相关的不可变数据及其字段约束。
 */
public record KnowledgeDocumentDTO(
        Long id,
        String title,
        String content,
        String docType,
        String source,
        List<Long> knowledgeIds) implements Serializable {}
