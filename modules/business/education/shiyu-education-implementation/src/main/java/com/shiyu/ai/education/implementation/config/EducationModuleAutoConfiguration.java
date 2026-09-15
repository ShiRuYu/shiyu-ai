package com.shiyu.ai.education.implementation.config;

import com.shiyu.ai.common.core.module.ConditionalOnBusinessModule;
import com.shiyu.ai.common.core.module.BusinessModuleDescriptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * 教育业务模块的可选装配入口。
 *
 * <p>平台只负责发现自动配置；教育模块自身负责装配自己的控制器、应用服务、持久化和数据库贡献者。
 * 这样新增业务模块时只需要增加同形态的模块入口，不需要扩大平台的根包扫描范围。
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
