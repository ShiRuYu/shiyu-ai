package com.shiyu.ai.common.mybatis.config;

import com.shiyu.ai.common.mybatis.tenant.ContextTenantFactory;

import com.mybatisflex.core.tenant.TenantManager;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;

/**
 * 定义 租户 Flex 基础设施或应用能力的配置项及装配规则。
 */
@Configuration
public class TenantFlexConfig {

    /**
     * 租户工厂，表示当前对象中的对应属性。
     */
    private final ContextTenantFactory tenantFactory;

    /**
     * 执行 租户 Flex 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantFactory 用于完成本次业务处理的 tenantFactory 参数。
     */
    public TenantFlexConfig(ContextTenantFactory tenantFactory) {
        this.tenantFactory = tenantFactory;
    }

    /**
     * 执行 租户 Flex 相关业务操作，并维护必要的状态和协作关系。
     */
    @PostConstruct
    public void init() {
        TenantManager.setTenantFactory(tenantFactory);
    }
}
