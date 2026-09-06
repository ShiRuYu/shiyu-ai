package com.shiyu.ai.common.core.config;

import com.shiyu.ai.common.core.domain.ShiYuDefaultExceptionHandler;
import com.shiyu.ai.common.core.tx.TransactionTemplateExecutor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

@AutoConfiguration
@EnableConfigurationProperties(ShiYuProperties.class)
public class ShiYuConfig {
    @Bean
    public ShiYuDefaultExceptionHandler defaultExceptionHandler() {
        return new ShiYuDefaultExceptionHandler();
    }
    @Bean
    @ConditionalOnBooleanProperty(prefix = "shiyu.tx", name = "enabled", havingValue = true)
    public TransactionTemplateExecutor transactionTemplateExecutor(PlatformTransactionManager transactionManager) {
        return new TransactionTemplateExecutor(transactionManager);
    }
}
