package com.shiyu.ai.common.mybatis.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DatabaseInfrastructureConfiguration 配置组件，负责注册和配置基础设施领域相关基础设施。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DatabaseInfrastructureProperties.class)
public class DatabaseInfrastructureConfiguration {

    /**
     * {@code databaseProviderValidator} 执行当前类型定义的业务操作。
     *
     * @param properties 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    public DatabaseProviderValidator databaseProviderValidator(
            DatabaseInfrastructureProperties properties) {
        properties.validate();
        return new DatabaseProviderValidator(properties.normalizedProvider());
    }

    /**
     * {@code DatabaseProviderValidator} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param provider 提供方，表示该记录组件承载的数据。
     */
    public record DatabaseProviderValidator(String provider) {}
}
