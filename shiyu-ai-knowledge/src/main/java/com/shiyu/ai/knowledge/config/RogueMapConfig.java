package com.shiyu.ai.knowledge.config;

import com.yomahub.roguemap.RogueMap;
import com.yomahub.roguemap.memory.RogueMemory;
import com.yomahub.roguemap.memory.SearchMode;
import com.yomahub.roguemap.embedding.UniversalEmbeddingProvider;
import com.yomahub.roguemap.serialization.StringCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * RogueMap 配置类
 * <p>
 * 支持多实例配置，通过配置文件控制启用哪些搜索模式：
 * <ul>
 *   <li>KEYWORD: 关键词搜索 (BM25)，默认启用</li>
 *   <li>SEMANTIC: 语义搜索 (向量 ANN)，默认禁用，需配置 embedding api-key</li>
 *   <li>HYBRID: 混合检索 (向量 + BM25 + RRF 融合)，默认启用，需配置 embedding api-key</li>
 * </ul>
 * <p>
 * 数据文件损坏检测由 {@link RogueMapFileManager} 在类加载时自动处理。
 */
@Slf4j
@Configuration
public class RogueMapConfig {

    @Value("${shiyu.data.dir:./data}")
    private String dataDir;

    @Value("${shiyu.knowledge.embedding.api-key:}")
    private String embeddingApiKey;

    /**
     * 关键词搜索实例 (BM25)
     * 默认启用，无需 Embedding API
     */
    @Bean(name = "knowledgeKeywordMemory")
    @ConditionalOnProperty(name = "shiyu.knowledge.search.keyword.enabled", havingValue = "true", matchIfMissing = true)
    public RogueMemory knowledgeKeywordMemory() {
        log.info("初始化关键词搜索实例 (KEYWORD), dataDir={}", dataDir);
        return RogueMemory.mmap()
                .persistent(dataDir + "/knowledge-keyword.db")
                .autoExpand(true)
                .autoCheckpoint(60, TimeUnit.SECONDS)
                .searchMode(SearchMode.KEYWORD_ONLY)
                .build();
    }

    /**
     * 语义搜索实例 (向量 ANN)
     * 默认禁用，需配置 embedding api-key
     */
    @Bean(name = "knowledgeSemanticMemory")
    @ConditionalOnProperty(name = "shiyu.knowledge.search.semantic.enabled", havingValue = "true")
    public RogueMemory knowledgeSemanticMemory() {
        if (embeddingApiKey == null || embeddingApiKey.isBlank()) {
            throw new IllegalStateException("语义搜索需要配置 embedding API key: shiyu.knowledge.embedding.api-key");
        }
        log.info("初始化语义搜索实例 (SEMANTIC), dataDir={}", dataDir);
        return RogueMemory.mmap()
                .persistent(dataDir + "/knowledge-semantic.db")
                .autoExpand(true)
                .autoCheckpoint(60, TimeUnit.SECONDS)
                .searchMode(SearchMode.VECTOR_ONLY)
                .embeddingProvider(new UniversalEmbeddingProvider(embeddingApiKey))
                .build();
    }

    /**
     * 混合检索实例 (向量 + BM25 + RRF 融合)
     * 默认启用，需配置 embedding api-key
     */
    @Bean(name = "knowledgeHybridMemory")
    @ConditionalOnProperty(name = "shiyu.knowledge.search.hybrid.enabled", havingValue = "true", matchIfMissing = true)
    public RogueMemory knowledgeHybridMemory() {
        if (embeddingApiKey == null || embeddingApiKey.isBlank()) {
            log.warn("混合检索需要配置 embedding API key，降级为关键词搜索");
            return knowledgeKeywordMemory();
        }
        log.info("初始化混合检索实例 (HYBRID), dataDir={}", dataDir);
        return RogueMemory.mmap()
                .persistent(dataDir + "/knowledge-hybrid.db")
                .autoExpand(true)
                .autoCheckpoint(60, TimeUnit.SECONDS)
                .searchMode(SearchMode.HYBRID)
                .embeddingProvider(new UniversalEmbeddingProvider(embeddingApiKey))
                .build();
    }

    /**
     * 知识图谱图存储 — RogueMap 堆外 KV
     */
    @Bean
    public RogueMap<String, String> graphRogueMap() {
        log.info("初始化知识图谱图存储, dataDir={}", dataDir);
        return RogueMap.<String, String>mmap()
                .persistent(dataDir + "/knowledge-graph.db")
                .autoExpand(true)
                .autoCheckpoint(60, TimeUnit.SECONDS)
                .keyCodec(StringCodec.INSTANCE)
                .valueCodec(StringCodec.INSTANCE)
                .build();
    }
}
