package com.shiyu.ai.common.core.config;

import com.shiyu.ai.common.core.tx.TransactionTemplateExecutor;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * {@code ShiYuConfig} 提供平台基础设施模块的配置项，并集中声明其默认值和运行约束。
 */
@AutoConfiguration
@EnableConfigurationProperties(ShiYuProperties.class)
public class ShiYuConfig {
    /**
     * {@code transactionTemplateExecutor} 执行当前类型定义的业务操作。
     *
     * @param transactionManager 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    @ConditionalOnBooleanProperty(prefix = "shiyu.tx", name = "enabled", havingValue = true)
    public TransactionTemplateExecutor transactionTemplateExecutor(
            PlatformTransactionManager transactionManager) {
        return new TransactionTemplateExecutor(transactionManager);
    }
}
