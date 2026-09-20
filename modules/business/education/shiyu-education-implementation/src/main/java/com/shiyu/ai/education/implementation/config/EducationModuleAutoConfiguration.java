package com.shiyu.ai.education.implementation.config;

import com.shiyu.ai.common.core.module.ConditionalOnBusinessModule;
import com.shiyu.ai.common.core.module.BusinessModuleDescriptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * 定义 教育 Module Auto 基础设施或应用能力的配置项及装配规则。
 */
@AutoConfiguration
@ConditionalOnBusinessModule(value = "education", matchIfMissing = true)
@ComponentScan(basePackages = "com.shiyu.ai.education.implementation")
public class EducationModuleAutoConfiguration {

    /**
     * 发布教育模块的路由和权限边界，供通用 Web 授权拦截器消费。
     *
     * @return 教育模块描述。
     */
    @Bean
    public BusinessModuleDescriptor educationModuleDescriptor() {
        return BusinessModuleDescriptor.of(
                "education", "教育", "/api/education", "education:");
    }
}
