package com.shiyu.ai.knowledge.contract.model;

import java.util.List;

/**
 * 封装 知识 Retrieval 相关的不可变数据及其字段约束。
 */
public record KnowledgeRetrievalResult(
        boolean success,
        List<KnowledgeRetrievalHit> hits,
        List<KnowledgeCitation> citations,
        String context,
        String errorMessage) {
    public KnowledgeRetrievalResult {
        hits = hits == null ? List.of() : List.copyOf(hits);
        citations = citations == null ? List.of() : List.copyOf(citations);
        context = context == null ? "" : context;
    }

    public static KnowledgeRetrievalResult failure(String message) {
        return new KnowledgeRetrievalResult(false, List.of(), List.of(), "", message);
    }
}
