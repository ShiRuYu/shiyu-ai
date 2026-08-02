package com.shiyu.ai.vector.config;

import com.shiyu.ai.vector.VectorStore;
import com.shiyu.ai.vector.VectorStoreOptions;
import com.shiyu.ai.vector.VectorStoreProvider;
import com.shiyu.ai.vector.factory.ConfiguredVectorStoreProvider;
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
    @ConditionalOnMissingBean(VectorStoreProvider.class)
    public VectorStoreProvider vectorStoreProvider(VectorStoreProperties properties) {
        log.info("创建 VectorStoreProvider: type={}", properties.getType());
        return new ConfiguredVectorStoreProvider(properties);
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(VectorStore.class)
    public VectorStore vectorStore(VectorStoreProvider provider, VectorStoreProperties properties) {
        log.info("创建默认 VectorStore: type={}, dataDir={}", provider.type(), properties.getResolvedDataDir());
        return provider.open(VectorStoreOptions.of(
                "global/default", properties.getDimension(), properties.getResolvedDataDir()));
    }
}
