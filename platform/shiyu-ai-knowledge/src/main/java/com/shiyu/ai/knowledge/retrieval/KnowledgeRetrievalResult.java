package com.shiyu.ai.knowledge.retrieval;

import java.util.List;

public record KnowledgeRetrievalResult(
        boolean success,
        List<KnowledgeRetrievalHit> hits,
        List<KnowledgeCitation> citations,
        String context,
        String errorMessage
) {
    public KnowledgeRetrievalResult {
        hits = hits == null ? List.of() : List.copyOf(hits);
        citations = citations == null ? List.of() : List.copyOf(citations);
        context = context == null ? "" : context;
    }

    public static KnowledgeRetrievalResult failure(String message) {
        return new KnowledgeRetrievalResult(false, List.of(), List.of(), "", message);
    }
}
