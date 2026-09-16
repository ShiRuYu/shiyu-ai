package com.shiyu.ai.composition.config;

import com.shiyu.ai.common.core.module.BusinessModuleProperties;
import com.shiyu.ai.common.mybatis.config.AgentDataSourceConfiguration;
import com.shiyu.ai.common.mybatis.config.ContextTenantFactory;
import com.shiyu.ai.common.mybatis.config.DatabaseInfrastructureConfiguration;
import com.shiyu.ai.common.mybatis.config.TenantFlexConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * 显式装配平台实现，避免启动类通过根包扫描意外加载可选业务模块。
 */
@AutoConfiguration
@EnableConfigurationProperties(BusinessModuleProperties.class)
@Import({
    AgentDataSourceConfiguration.class,
    ContextTenantFactory.class,
    DatabaseInfrastructureConfiguration.class,
    TenantFlexConfig.class
})
@ComponentScan(
        basePackages = {
            "com.shiyu.ai.composition",
            "com.shiyu.ai.iam.implementation",
            "com.shiyu.ai.agent.implementation",
            "com.shiyu.ai.conversation.implementation",
            "com.shiyu.ai.model.implementation",
            "com.shiyu.ai.memory.implementation",
            "com.shiyu.ai.knowledge.implementation",
            "com.shiyu.ai.tooling.implementation",
            "com.shiyu.ai.governance.implementation",
            "com.shiyu.ai.common.web",
            "com.shiyu.ai.common.storage",
            "com.shiyu.ai.web"
        })
public class PlatformCompositionAutoConfiguration {}
