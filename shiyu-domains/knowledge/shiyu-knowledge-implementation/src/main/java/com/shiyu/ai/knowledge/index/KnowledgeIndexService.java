package com.shiyu.ai.knowledge.index;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface KnowledgeIndexService extends FullTextIndex, VectorIndex {

    long rebuild(TenantId tenantId, Long spaceId);

    default List<HybridHit> hybridSearch(TenantId tenantId, Long spaceId, String query,
                                         int topK, boolean rerank) {
        return hybridSearch(tenantId, spaceId, query, "HYBRID", topK, 0D, rerank);
    }

    List<HybridHit> hybridSearch(TenantId tenantId, Long spaceId, String query,
                                 String mode, int topK, double threshold, boolean rerank);

    /** User-facing semantic retrieval must preserve the full actor boundary. */
    default List<HybridHit> hybridSearch(ActorContext actor, Long spaceId, String query,
                                         String mode, int topK, double threshold, boolean rerank) {
        if (actor == null) {
            throw new IllegalArgumentException("actor is required");
        }
        return hybridSearch(actor.tenantId(), spaceId, query, mode, topK, threshold, rerank);
    }

    record HybridHit(Long chunkId, Long documentId, String content, String highlight,
                     double bm25Score, double vectorScore, double rrfScore,
                     double rerankScore) {
    }
}
