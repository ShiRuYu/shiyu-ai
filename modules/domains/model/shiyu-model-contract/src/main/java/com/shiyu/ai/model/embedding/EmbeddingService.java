package com.shiyu.ai.model.embedding;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/** Provider-neutral embedding contract consumed by Knowledge. */
public interface EmbeddingService {
    float[] embed(TenantId tenantId, String text);

    default float[] embed(ActorContext actor, String text) {
        if (actor == null) {
            throw new IllegalArgumentException("actor is required");
        }
        return embed(actor.tenantId(), text);
    }

    List<float[]> embedBatch(TenantId tenantId, List<String> texts);

    int dimension();
}
