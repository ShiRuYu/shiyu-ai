package com.shiyu.ai.knowledge.implementation.domain.model;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;

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
