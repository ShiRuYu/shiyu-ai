package com.shiyu.ai.knowledge.contract.model;

import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;
import java.util.Set;

/**
 * {@code KnowledgeRetrievalRequest} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param accessContext accessContext 属性，表示该记录组件承载的数据。
 * @param spaceIds spaceIds 属性，表示该记录组件承载的数据。
 * @param sourceTypes sourceTypes 属性，表示该记录组件承载的数据。
 * @param retrievalMode retrievalMode 属性，表示该记录组件承载的数据。
 * @param query query 属性，表示该记录组件承载的数据。
 * @param candidateTopK candidateTopK 属性，表示该记录组件承载的数据。
 * @param topK topK 属性，表示该记录组件承载的数据。
 * @param scoreThreshold scoreThreshold 属性，表示该记录组件承载的数据。
 * @param enableRerank enableRerank 属性，表示该记录组件承载的数据。
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
