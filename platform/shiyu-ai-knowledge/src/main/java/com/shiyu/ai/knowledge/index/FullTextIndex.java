package com.shiyu.ai.knowledge.index;

import java.util.List;

public interface FullTextIndex {

    List<FullTextHit> search(Long tenantId, Long spaceId, Long version,
                             String query, int topK);

    record FullTextHit(Long chunkId, Long documentId, float score, String highlight) {
    }
}
