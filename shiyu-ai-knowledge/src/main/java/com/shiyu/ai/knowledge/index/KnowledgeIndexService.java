package com.shiyu.ai.knowledge.index;

import java.util.List;

public interface KnowledgeIndexService extends FullTextIndex, VectorIndex {

    long rebuild(Long tenantId, Long spaceId);

    List<HybridHit> hybridSearch(Long tenantId, Long spaceId, String query,
                                 int topK, boolean rerank);

    record HybridHit(Long chunkId, Long documentId, String content, String highlight,
                     double bm25Score, double vectorScore, double rrfScore,
                     double rerankScore) {
    }
}
