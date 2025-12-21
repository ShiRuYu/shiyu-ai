package com.shiyu.ai.common.core.config;

import com.shiyu.ai.common.core.domain.ShiYuDefaultExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(ShiYuProperties.class)
public class ShiYuConfig {
    @Bean
    public ShiYuDefaultExceptionHandler defaultExceptionHandler() {
        return new ShiYuDefaultExceptionHandler();
    }
}
