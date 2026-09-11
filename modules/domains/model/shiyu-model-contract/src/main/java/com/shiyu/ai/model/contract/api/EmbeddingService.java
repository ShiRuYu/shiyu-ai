package com.shiyu.ai.model.contract.api;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/** Provider-neutral embedding contract consumed by Knowledge. */
public interface EmbeddingService {
    /** Creates one tenant-scoped embedding for the supplied text. */
    float[] embed(TenantId tenantId, String text);

    default float[] embed(ActorContext actor, String text) {
        if (actor == null) {
            throw new IllegalArgumentException("actor is required");
        }
        return embed(actor.tenantId(), text);
    }

    /** Creates embeddings for a batch while preserving input order. */
    List<float[]> embedBatch(TenantId tenantId, List<String> texts);

    /** Returns the vector dimension produced by this provider. */
    int dimension();
}
