package com.shiyu.ai.memory.config;

import com.yomahub.roguemap.embedding.UniversalEmbeddingProvider;
import com.yomahub.roguemap.memory.RogueMemory;
import com.yomahub.roguemap.memory.SearchMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class RogueMemoryConfig {

    @Value("${rogue.memory.embedding.api-key:}")
    private String embeddingApiKey;

    @Value("${rogue.memory.search-mode:KEYWORD_ONLY}")
    private String searchMode;

    /**
     * AI 记忆层 — RogueMemory
     * 提供向量 ANN (HNSW) + BM25 混合检索能力
     *
     * 三种检索模式:
     * - HYBRID       → 向量 + BM25 双通道 (需配置 embedding.api-key)
     * - VECTOR_ONLY  → 仅向量检索 (需配置 embedding.api-key)
     * - KEYWORD_ONLY → 纯 BM25 关键词检索 (默认，无需 Embedding API)
     */
    @Bean
    @ConditionalOnMissingBean
    public RogueMemory rogueMemory() {
        RogueMemory.MmapBuilder builder = RogueMemory.mmap()
                .persistent("./data/ai-memory")
                .autoCheckpoint(60, TimeUnit.SECONDS)
                .searchMode(SearchMode.valueOf(searchMode));

        if (!"KEYWORD_ONLY".equals(searchMode)
                && embeddingApiKey != null && !embeddingApiKey.isBlank()) {
            builder.embeddingProvider(new UniversalEmbeddingProvider(embeddingApiKey));
        }

        return builder.build();
    }
}
