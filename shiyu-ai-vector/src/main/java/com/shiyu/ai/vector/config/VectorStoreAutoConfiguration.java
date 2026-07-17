package com.shiyu.ai.vector.config;

import com.shiyu.ai.vector.factory.VectorStoreFactory;
import com.shiyu.ai.vector.VectorStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * VectorStore 自动配置
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(VectorStoreProperties.class)
public class VectorStoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VectorStore vectorStore(VectorStoreProperties properties) {
        log.info("创建 VectorStore: type={}", properties.getType());
        return VectorStoreFactory.create(properties.getType(), properties);
    }
}
