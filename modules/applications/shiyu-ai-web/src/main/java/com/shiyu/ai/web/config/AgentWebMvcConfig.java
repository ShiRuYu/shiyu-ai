package com.shiyu.ai.web.config;

import com.shiyu.ai.agent.implementation.service.AuditService;
import com.shiyu.ai.common.web.auth.ClientIpResolver;
import com.shiyu.ai.web.interceptor.AuditInterceptor;

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

    /**
     * 审计服务，表示当前对象中的对应属性。
     */
    private final AuditService auditService;
    /**
     * clientIpResolver 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ClientIpResolver clientIpResolver;

    /**
     * {@code AgentWebMvcConfig} 创建并初始化当前类型实例。
     *
     * @param auditService 参数值，用于执行当前操作。
     * @param clientIpResolver 参数值，用于执行当前操作。
     */
    public AgentWebMvcConfig(AuditService auditService, ClientIpResolver clientIpResolver) {
        this.auditService = auditService;
        this.clientIpResolver = clientIpResolver;
    }

    /**
     * {@code addInterceptors} 执行当前类型定义的业务操作。
     *
     * @param registry 参数值，用于执行当前操作。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuditInterceptor(auditService, clientIpResolver))
                .addPathPatterns("/api/**");
    }
}
