package com.shiyu.ai.knowledge.config;

import com.yomahub.roguemap.RogueMap;
import com.yomahub.roguemap.memory.RogueMemory;
import com.yomahub.roguemap.memory.SearchMode;
import com.yomahub.roguemap.embedding.UniversalEmbeddingProvider;
import com.yomahub.roguemap.serialization.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class RogueMapConfig {

    @Value("${rogue.memory.embedding.api-key:}")
    private String embeddingApiKey;

    /**
     * 知识点语义搜索 — RogueMemory
     * 提供向量 ANN (HNSW) + BM25 混合检索，用于知识点的语义搜索和推荐
     *
     * 三种检索模式:
     * - HYBRID       → 向量 + BM25 双通道 (需配置 embedding.api-key)
     * - VECTOR_ONLY  → 仅向量检索
     * - KEYWORD_ONLY → 纯 BM25 关键词检索 (默认，无需 Embedding API)
     */
    @Bean(name = "knowledgeRogueMemory")
    @ConditionalOnMissingBean(name = "knowledgeRogueMemory")
    public RogueMemory knowledgeRogueMemory() {
        String mode = embeddingApiKey != null && !embeddingApiKey.isBlank()
                ? "HYBRID" : "KEYWORD_ONLY";

        RogueMemory.MmapBuilder builder = RogueMemory.mmap()
                .persistent("./data/knowledge-vectors.db")
                .autoExpand(true)
                .autoCheckpoint(60, TimeUnit.SECONDS)
                .searchMode(SearchMode.valueOf(mode));

        if (!"KEYWORD_ONLY".equals(mode)) {
            builder.embeddingProvider(new UniversalEmbeddingProvider(embeddingApiKey));
        }

        return builder.build();
    }

    /**
     * 知识图谱图存储 — RogueMap 堆外 KV
     */
    @Bean
    @ConditionalOnMissingBean(name = "graphRogueMap")
    public RogueMap<String, String> graphRogueMap() {
        return RogueMap.<String, String>mmap()
                .persistent("./data/knowledge-graph.db")
                .autoExpand(true)
                .autoCheckpoint(60, TimeUnit.SECONDS)
                .keyCodec(StringCodec.INSTANCE)
                .valueCodec(StringCodec.INSTANCE)
                .build();
    }
}
