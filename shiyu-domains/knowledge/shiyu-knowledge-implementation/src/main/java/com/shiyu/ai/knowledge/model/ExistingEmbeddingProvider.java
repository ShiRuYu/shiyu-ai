package com.shiyu.ai.knowledge.model;

import com.shiyu.ai.model.embedding.EmbeddingService;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.ActorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@RequiredArgsConstructor
public class ExistingEmbeddingProvider implements EmbeddingProvider {

    private final EmbeddingService embeddingService;

    @Override
    public String profile() {
        return "default";
    }

    @Override
    public float[] embed(TenantId tenantId, String text) {
        return embeddingService.embed(tenantId, text);
    }

    @Override
    public float[] embed(ActorContext actor, String text) {
        return embeddingService.embed(actor, text);
    }
}
