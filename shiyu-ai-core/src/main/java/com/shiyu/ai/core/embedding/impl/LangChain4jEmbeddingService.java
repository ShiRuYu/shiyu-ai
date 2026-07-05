package com.shiyu.ai.core.embedding.impl;

import com.shiyu.ai.core.embedding.EmbeddingService;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class LangChain4jEmbeddingService implements EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public LangChain4jEmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
        log.info("EmbeddingService 初始化完成, 维度={}", dimension());
    }

    public LangChain4jEmbeddingService() {
        this(new AllMiniLmL6V2EmbeddingModel());
    }

    @Override
    public float[] embed(String text) {
        Embedding embedding = embeddingModel.embed(text).content();
        return embedding.vector();
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        List<TextSegment> segments = texts.stream()
                .map(TextSegment::from)
                .toList();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        return embeddings.stream()
                .map(Embedding::vector)
                .toList();
    }

    @Override
    public int dimension() {
        return embeddingModel.dimension();
    }
}
