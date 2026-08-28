package com.shiyu.ai.knowledge.index;

import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface FullTextIndex {

    List<FullTextHit> search(TenantId tenantId, Long spaceId, Long version,
                             String query, int topK);

    record FullTextHit(Long chunkId, Long documentId, float score, String highlight) {
    }
}
