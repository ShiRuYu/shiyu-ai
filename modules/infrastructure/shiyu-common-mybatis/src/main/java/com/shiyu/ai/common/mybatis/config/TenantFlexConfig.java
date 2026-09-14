package com.shiyu.ai.common.mybatis.config;

import com.mybatisflex.core.tenant.TenantManager;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;

/** MyBatis-Flex 多租户配置 注册基于登录上下文的 TenantFactory */
@Configuration
public class TenantFlexConfig {

    /**
     * 租户工厂，表示当前对象中的对应属性。
     */
    private final ContextTenantFactory tenantFactory;

    /**
     * {@code TenantFlexConfig} 创建并初始化当前类型实例。
     *
     * @param tenantFactory 参数值，用于执行当前操作。
     */
    public TenantFlexConfig(ContextTenantFactory tenantFactory) {
        this.tenantFactory = tenantFactory;
    }

    /**
     * {@code init} 执行当前类型定义的业务操作。
     */
    @PostConstruct
    public void init() {
        TenantManager.setTenantFactory(tenantFactory);
    }
}
