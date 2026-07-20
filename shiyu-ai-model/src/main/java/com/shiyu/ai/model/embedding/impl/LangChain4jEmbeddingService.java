package com.shiyu.ai.model.embedding.impl;

import com.shiyu.ai.model.embedding.EmbeddingService;
import com.shiyu.ai.model.event.EmbeddingCallEvent;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzhv15.BgeSmallZhV15EmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class LangChain4jEmbeddingService implements EmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final ApplicationEventPublisher eventPublisher;

    public LangChain4jEmbeddingService(EmbeddingModel embeddingModel,
                                       ApplicationEventPublisher eventPublisher) {
        this.embeddingModel = embeddingModel;
        this.eventPublisher = eventPublisher;
        log.info("EmbeddingService 初始化完成, 维度={}", dimension());
    }

    // 保留无参构造用于手动测试场景
    public LangChain4jEmbeddingService() {
        this(new BgeSmallZhV15EmbeddingModel(), event -> {});
    }

    @Override
    public float[] embed(String text) {
        long startMs = System.currentTimeMillis();
        Embedding embedding = embeddingModel.embed(text).content();
        long latencyMs = System.currentTimeMillis() - startMs;

        int estimatedTokens = estimateTokens(text);
        eventPublisher.publishEvent(new EmbeddingCallEvent(
                "BGE-small-zh-v1.5", text.length(), estimatedTokens, 1, latencyMs));

        log.debug("Embedding 完成: textLen={}, tokens≈{}, latency={}ms",
                text.length(), estimatedTokens, latencyMs);
        return embedding.vector();
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        long startMs = System.currentTimeMillis();
        List<TextSegment> segments = texts.stream()
                .map(TextSegment::from)
                .toList();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        long latencyMs = System.currentTimeMillis() - startMs;

        int totalTextLen = texts.stream().mapToInt(String::length).sum();
        int estimatedTokens = estimateTokens(String.join("", texts));
        eventPublisher.publishEvent(new EmbeddingCallEvent(
                "BGE-small-zh-v1.5", totalTextLen, estimatedTokens, texts.size(), latencyMs));

        log.debug("Embedding 批处理完成: batchSize={}, totalLen={}, tokens≈{}, latency={}ms",
                texts.size(), totalTextLen, estimatedTokens, latencyMs);
        return embeddings.stream()
                .map(Embedding::vector)
                .toList();
    }

    @Override
    public int dimension() {
        return embeddingModel.dimension();
    }

    /**
     * 估算 Token 数量（中文约 1 token/1.5 字，英文约 1 token/4 字符）
     */
    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        int chineseChars = 0;
        int otherChars = 0;
        for (char c : text.toCharArray()) {
            if (c >= 0x4E00 && c <= 0x9FFF) {
                chineseChars++;
            } else {
                otherChars++;
            }
        }
        return (int) Math.ceil(chineseChars / 1.5 + otherChars / 4.0);
    }
}
