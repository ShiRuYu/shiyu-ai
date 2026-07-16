package com.shiyu.ai.agent.config;

import com.shiyu.ai.agent.service.AuditService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Agent 模块 Web MVC 配置
 * <p>
 * 注册 Agent 模块特有的拦截器（审计日志等）。
 */
@AutoConfiguration
public class AgentWebMvcConfig implements WebMvcConfigurer {

    private final AuditService auditService;

    public AgentWebMvcConfig(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuditInterceptor(auditService))
                .addPathPatterns("/api/**");
    }
}
