package com.shiyu.ai.knowledge.model;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.ActorContext;

public interface EmbeddingProvider {
    String profile();
    float[] embed(TenantId tenantId, String text);

    /** Actor-aware variant used by user initiated semantic retrieval. */
    default float[] embed(ActorContext actor, String text) {
        if (actor == null) {
            throw new IllegalArgumentException("actor is required");
        }
        return embed(actor.tenantId(), text);
    }
}
