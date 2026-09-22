package com.shiyu.ai.common.foundation.config;

import com.shiyu.ai.common.foundation.tx.TransactionTemplateExecutor;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 定义 Shi Yu 基础设施或应用能力的配置项及装配规则。
 */
@AutoConfiguration
@EnableConfigurationProperties(ShiYuProperties.class)
public class ShiYuConfig {
    /**
     * 执行 Shi Yu 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tx 用于完成本次业务处理的 tx 参数。
     * @param enabled 用于完成本次业务处理的 enabled 参数。
     * @param true 用于完成本次业务处理的 true 参数。
     */
    @Bean
    @ConditionalOnBooleanProperty(prefix = "shiyu.tx", name = "enabled", havingValue = true)
    public TransactionTemplateExecutor transactionTemplateExecutor(
            PlatformTransactionManager transactionManager) {
        return new TransactionTemplateExecutor(transactionManager);
    }
}
