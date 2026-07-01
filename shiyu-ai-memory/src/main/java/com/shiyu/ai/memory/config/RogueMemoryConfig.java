package com.shiyu.ai.memory.config;

import com.yomahub.roguemap.embedding.UniversalEmbeddingProvider;
import com.yomahub.roguemap.memory.RogueMemory;
import com.yomahub.roguemap.memory.SearchMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class RogueMemoryConfig {

    @Value("${shiyu.data.roguemap:${app.home}/data/roguemap}")
    private String dataDir;

    @Value("${rogue.memory.embedding.api-key:}")
    private String embeddingApiKey;

    @Value("${rogue.memory.search-mode:KEYWORD_ONLY}")
    private String searchMode;

    @Bean
    @ConditionalOnMissingBean
    public RogueMemory rogueMemory() {
        log.info("初始化 AI 记忆 RogueMemory, dataDir={}", dataDir);
        RogueMemory.MmapBuilder builder = RogueMemory.mmap()
                .persistent(dataDir + "/ai-memory")
                .autoCheckpoint(60, TimeUnit.SECONDS)
                .searchMode(SearchMode.valueOf(searchMode));

        if (!"KEYWORD_ONLY".equals(searchMode)
                && embeddingApiKey != null && !embeddingApiKey.isBlank()) {
            builder.embeddingProvider(new UniversalEmbeddingProvider(embeddingApiKey));
        }

        return builder.build();
    }
}
