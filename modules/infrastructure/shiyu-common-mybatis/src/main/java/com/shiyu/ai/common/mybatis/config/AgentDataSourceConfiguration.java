package com.shiyu.ai.common.mybatis.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 定义 智能体 Data Source 基础设施或应用能力的配置项及装配规则。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(name = "agentDataSource")
public class AgentDataSourceConfiguration {

    /**
     * 执行 智能体 Data Source 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param agent 用于完成本次业务处理的 agent 参数。
     */
    @Bean
    @ConfigurationProperties(prefix = "mybatis-flex.datasource.agent")
    public DataSourceProperties agentDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * 执行 智能体 Data Source 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param agentDataSource 用于完成本次业务处理的 agentDataSource 参数。
     */
    @Bean(name = "agentDataSource")
    @Primary
    public DataSource agentDataSource(DataSourceProperties agentDataSourceProperties) {
        return agentDataSourceProperties.initializeDataSourceBuilder().build();
    }
}
