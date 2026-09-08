package com.shiyu.ai.knowledge.index;

import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface VectorIndex {

    List<VectorHit> search(TenantId tenantId, Long spaceId, Long version,
                           float[] queryVector, int topK);

    record VectorHit(Long chunkId, double score) {
    }
}
