package com.shiyu.ai.knowledge.contract.model;

import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;
import java.util.Set;

/**
 * 封装 知识 Retrieval 相关的不可变数据及其字段约束。
 */
public record KnowledgeRetrievalRequest(
        ActorContext accessContext,
        List<Long> spaceIds,
        Set<KnowledgeSourceType> sourceTypes,
        RetrievalMode retrievalMode,
        String query,
        Integer candidateTopK,
        Integer topK,
        Double scoreThreshold,
        Boolean enableRerank) {
    public KnowledgeRetrievalRequest {
        spaceIds = spaceIds == null ? List.of() : List.copyOf(spaceIds);
        sourceTypes =
                sourceTypes == null || sourceTypes.isEmpty()
                        ? Set.of(KnowledgeSourceType.DOCUMENT, KnowledgeSourceType.KNOWLEDGE_ENTRY)
                        : Set.copyOf(sourceTypes);
        retrievalMode = retrievalMode == null ? RetrievalMode.HYBRID : retrievalMode;
        candidateTopK = candidateTopK == null ? 20 : candidateTopK;
        topK = topK == null ? 5 : topK;
        scoreThreshold = scoreThreshold == null ? 0D : scoreThreshold;
        enableRerank = enableRerank == null || enableRerank;
    }
}
