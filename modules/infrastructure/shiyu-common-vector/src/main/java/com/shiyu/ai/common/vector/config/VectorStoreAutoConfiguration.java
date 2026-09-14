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

/** VectorStore 自动配置 */
@Slf4j
@Configuration
@EnableConfigurationProperties({VectorStoreProperties.class, VectorInfrastructureProperties.class})
public class VectorStoreAutoConfiguration {

    /**
     * {@code vectorStoreProvider} 执行当前类型定义的业务操作。
     *
     * @param properties 参数值，用于执行当前操作。
     * @param infrastructureProperties 参数值，用于执行当前操作。
     * @param jdbcTemplates 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code vectorStore} 执行当前类型定义的业务操作。
     *
     * @param provider 参数值，用于执行当前操作。
     * @param properties 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
