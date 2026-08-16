package com.shiyu.ai.dal.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Registers the single local application data source used by the platform repositories.
 *
 * <p>The MyBatis-Flex datasource properties are intentionally kept as the public
 * configuration contract.  Some Boot 4 starter combinations no longer expose the
 * named datasource bean expected by the JDBC repositories, so the platform owns this
 * small binding explicitly.  The conditional keeps deployments that provide their own
 * datasource implementation (for example the future P3 adapters) in control.</p>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(DataSource.class)
public class AgentDataSourceConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "mybatis-flex.datasource.agent")
    public DataSourceProperties agentDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "agentDataSource")
    public DataSource agentDataSource(DataSourceProperties agentDataSourceProperties) {
        return agentDataSourceProperties.initializeDataSourceBuilder().build();
    }
}
