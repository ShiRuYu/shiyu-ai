package com.shiyu.ai.common.mybatis.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * AgentDataSourceConfiguration 配置组件，负责注册和配置基础设施领域相关基础设施。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(DataSource.class)
public class AgentDataSourceConfiguration {

    /**
     * {@code agentDataSourceProperties} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    @ConfigurationProperties(prefix = "mybatis-flex.datasource.agent")
    public DataSourceProperties agentDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * {@code agentDataSource} 执行当前类型定义的业务操作。
     *
     * @param agentDataSourceProperties 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean(name = "agentDataSource")
    public DataSource agentDataSource(DataSourceProperties agentDataSourceProperties) {
        return agentDataSourceProperties.initializeDataSourceBuilder().build();
    }
}
