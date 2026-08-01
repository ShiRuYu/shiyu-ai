package com.shiyu.ai.knowledge.index;

import java.util.List;

public interface KnowledgeIndexService extends FullTextIndex, VectorIndex {

    long rebuild(Long tenantId, Long spaceId);

    default List<HybridHit> hybridSearch(Long tenantId, Long spaceId, String query,
                                         int topK, boolean rerank) {
        return hybridSearch(tenantId, spaceId, query, "HYBRID", topK, 0D, rerank);
    }

    List<HybridHit> hybridSearch(Long tenantId, Long spaceId, String query,
                                 String mode, int topK, double threshold, boolean rerank);

    record HybridHit(Long chunkId, Long documentId, String content, String highlight,
                     double bm25Score, double vectorScore, double rrfScore,
                     double rerankScore) {
    }
}
