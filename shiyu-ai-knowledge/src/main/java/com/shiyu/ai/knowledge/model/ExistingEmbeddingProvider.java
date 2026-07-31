package com.shiyu.ai.knowledge.model;

import com.shiyu.ai.model.embedding.EmbeddingService;
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
    public float[] embed(String text) {
        return embeddingService.embed(text);
    }
}
