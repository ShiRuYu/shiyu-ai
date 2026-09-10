package com.shiyu.ai.common.mybatis.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Validates the selected database provider before repository initialization. */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DatabaseInfrastructureProperties.class)
public class DatabaseInfrastructureConfiguration {

    @Bean
    public DatabaseProviderValidator databaseProviderValidator(
            DatabaseInfrastructureProperties properties) {
        properties.validate();
        return new DatabaseProviderValidator(properties.normalizedProvider());
    }

    public record DatabaseProviderValidator(String provider) {
    }
}
