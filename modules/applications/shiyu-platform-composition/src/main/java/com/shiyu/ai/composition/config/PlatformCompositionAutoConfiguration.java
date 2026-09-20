package com.shiyu.ai.composition.config;

import com.shiyu.ai.common.mybatis.config.AgentDataSourceConfiguration;
import com.shiyu.ai.common.mybatis.tenant.ContextTenantFactory;
import com.shiyu.ai.common.mybatis.config.DatabaseInfrastructureConfiguration;
import com.shiyu.ai.common.mybatis.config.TenantFlexConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * 定义 平台 Composition Auto 基础设施或应用能力的配置项及装配规则。
 */
@AutoConfiguration
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
