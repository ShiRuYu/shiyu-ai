package com.shiyu.ai.knowledge.config;

import com.shiyu.ai.vector.spi.VectorStore;
import com.shiyu.ai.vector.config.VectorStoreProperties;
import com.shiyu.ai.vector.factory.VectorStoreFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * VectorStore 自动配置（兼容适配层）
 * 优先使用 shiyu-ai-vector 模块的自动配置
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(VectorStoreProperties.class)
public class VectorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VectorStore vectorStore(VectorStoreProperties properties) {
        log.info("创建 VectorStore: type={}", properties.getType());
        return VectorStoreFactory.create(properties.getType(), properties);
    }
}
