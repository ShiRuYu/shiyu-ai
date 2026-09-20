package com.shiyu.ai.common.vector.config;

import com.shiyu.ai.common.vector.api.VectorStore;
import com.shiyu.ai.common.vector.api.VectorStoreProvider;
import com.shiyu.ai.common.vector.factory.ConfiguredVectorStoreProvider;
import com.shiyu.ai.common.vector.model.VectorStoreOptions;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;

/**
 * 定义 向量 Store Auto 基础设施或应用能力的配置项及装配规则。
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({VectorStoreProperties.class, VectorInfrastructureProperties.class})
public class VectorStoreAutoConfiguration {

    /**
     * 执行 向量 Store Auto 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Bean
    @ConditionalOnMissingBean(VectorStoreProvider.class)
    public VectorStoreProvider vectorStoreProvider(
            VectorStoreProperties properties,
            VectorInfrastructureProperties infrastructureProperties,
            ObjectProvider<JdbcTemplate> jdbcTemplates) {
        properties.setType(infrastructureProperties.resolveProvider(properties.getType()));
        JdbcTemplate jdbc = jdbcTemplates.getIfAvailable();
        validateProviderDataSource(properties.getType(), jdbc);
        log.info("创建 VectorStoreProvider: type={}", properties.getType());
        return new ConfiguredVectorStoreProvider(properties, jdbc);
    }

    /**
     * 执行 向量 Store Auto 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param close 用于完成本次业务处理的 close 参数。
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(VectorStore.class)
    public VectorStore vectorStore(VectorStoreProvider provider, VectorStoreProperties properties) {
        log.info(
                "创建默认 VectorStore: type={}, dataDirPresent={}",
                provider.type(),
                properties.getResolvedDataDir() != null);
        return provider.open(
                VectorStoreOptions.of(
                        "global/default",
                        properties.getDimension(),
                        properties.getResolvedDataDir()));
    }

    private void validateProviderDataSource(String provider, JdbcTemplate jdbc) {
        if (!"pgvector".equalsIgnoreCase(provider)) {
            return;
        }
        if (jdbc == null || jdbc.getDataSource() == null) {
            throw new IllegalStateException("pgvector requires a PostgreSQL DataSource");
        }
        try (var connection = jdbc.getDataSource().getConnection()) {
            String product = connection.getMetaData().getDatabaseProductName();
            if (product == null
                    || !product.toLowerCase(java.util.Locale.ROOT).contains("postgresql")) {
                throw new IllegalStateException(
                        "pgvector requires PostgreSQL; actual database=" + product);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Unable to validate pgvector PostgreSQL connection", exception);
        }
    }
}
