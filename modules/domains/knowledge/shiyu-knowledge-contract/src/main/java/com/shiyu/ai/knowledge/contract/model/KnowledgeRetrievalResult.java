package com.shiyu.ai.knowledge.contract.model;

import java.util.List;

/**
 * {@code KnowledgeRetrievalResult} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param success success 属性，表示该记录组件承载的数据。
 * @param hits hits 属性，表示该记录组件承载的数据。
 * @param citations citations 属性，表示该记录组件承载的数据。
 * @param context 上下文，表示该记录组件承载的数据。
 * @param errorMessage errorMessage 属性，表示该记录组件承载的数据。
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
