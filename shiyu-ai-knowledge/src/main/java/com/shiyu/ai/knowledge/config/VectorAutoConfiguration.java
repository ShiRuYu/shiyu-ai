package com.shiyu.ai.knowledge.config;

import com.shiyu.ai.core.embedding.EmbeddingService;
import com.shiyu.ai.core.embedding.impl.LangChain4jEmbeddingService;
import com.shiyu.ai.knowledge.vector.VectorStore;
import com.shiyu.ai.knowledge.vector.VectorStoreFactory;
import com.shiyu.ai.knowledge.vector.config.VectorStoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(VectorStoreProperties.class)
public class VectorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EmbeddingService embeddingService() {
        log.info("创建默认 EmbeddingService (AllMiniLmL6V2)");
        return new LangChain4jEmbeddingService();
    }

    @Bean
    @ConditionalOnMissingBean
    public VectorStore vectorStore(VectorStoreProperties properties) {
        log.info("创建 VectorStore: type={}", properties.getType());
        return VectorStoreFactory.create(properties.getType(), properties);
    }
}
