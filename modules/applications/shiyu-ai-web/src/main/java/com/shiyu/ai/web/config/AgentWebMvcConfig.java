package com.shiyu.ai.web.config;

import com.shiyu.ai.agent.implementation.service.AuditService;
import com.shiyu.ai.common.web.auth.ClientIpResolver;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Agent 模块 Web MVC 配置
 *
 * <p>注册 Agent 模块特有的拦截器（审计日志等）。
 */
@AutoConfiguration
public class AgentWebMvcConfig implements WebMvcConfigurer {

    private final AuditService auditService;
    private final ClientIpResolver clientIpResolver;

    public AgentWebMvcConfig(AuditService auditService, ClientIpResolver clientIpResolver) {
        this.auditService = auditService;
        this.clientIpResolver = clientIpResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuditInterceptor(auditService, clientIpResolver))
                .addPathPatterns("/api/**");
    }
}
