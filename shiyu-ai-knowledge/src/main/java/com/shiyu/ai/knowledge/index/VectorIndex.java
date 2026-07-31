package com.shiyu.ai.knowledge.index;

import java.util.List;

public interface VectorIndex {

    List<VectorHit> search(Long tenantId, Long spaceId, Long version,
                           float[] queryVector, int topK);

    record VectorHit(Long chunkId, double score) {
    }
}
