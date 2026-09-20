package com.shiyu.ai.web.config;

import com.shiyu.ai.common.core.module.BusinessModuleDescriptor;
import com.shiyu.ai.iam.contract.module.TenantModuleAccessPort;
import com.shiyu.ai.web.interceptor.BusinessModuleAccessInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 定义 Business Module Access Web 基础设施或应用能力的配置项及装配规则。
 */
@AutoConfiguration
public class BusinessModuleAccessWebConfig {

    /**
     * 创建模块授权拦截器 Bean。
     *
     * @param modules 进程中已启用的业务模块。
     * @param access IAM 租户模块授权契约。
     * @return 模块授权拦截器。
     */
    @Bean
    public BusinessModuleAccessInterceptor businessModuleAccessInterceptor(
            List<BusinessModuleDescriptor> modules, TenantModuleAccessPort access) {
        return new BusinessModuleAccessInterceptor(modules, access);
    }
}
