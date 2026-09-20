package com.shiyu.ai.common.mybatis.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 定义 数据库 Infrastructure 基础设施或应用能力的配置项及装配规则。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DatabaseInfrastructureProperties.class)
public class DatabaseInfrastructureConfiguration {

    /**
     * 执行 数据库 Infrastructure 相关业务数据，并返回处理结果。
     *
     * @param properties 用于完成本次业务处理的 properties 参数。
     * @return 返回 数据库 Infrastructure 相关操作生成的结果数据。
     */
    @Bean
    public DatabaseProviderValidator databaseProviderValidator(
            DatabaseInfrastructureProperties properties) {
        properties.validate();
        return new DatabaseProviderValidator(properties.normalizedProvider());
    }

    /**
     * 封装 数据库 Provider 相关的不可变数据及其字段约束。
     */
    public record DatabaseProviderValidator(String provider) {}
}
