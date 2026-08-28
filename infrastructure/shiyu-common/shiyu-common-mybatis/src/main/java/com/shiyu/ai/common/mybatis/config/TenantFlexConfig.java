package com.shiyu.ai.common.mybatis.config;

import com.mybatisflex.core.tenant.TenantManager;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * MyBatis-Flex 多租户配置
 * 注册基于登录上下文的 TenantFactory
 */
@Configuration
public class TenantFlexConfig {

    private final ContextTenantFactory tenantFactory;

    public TenantFlexConfig(ContextTenantFactory tenantFactory) {
        this.tenantFactory = tenantFactory;
    }

    @PostConstruct
    public void init() {
        TenantManager.setTenantFactory(tenantFactory);
    }
}
