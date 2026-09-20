package com.shiyu.ai.web.config;

import com.shiyu.ai.agent.implementation.service.AuditService;
import com.shiyu.ai.common.web.auth.ClientIpResolver;
import com.shiyu.ai.web.interceptor.AuditInterceptor;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 定义 智能体 Web Mvc 基础设施或应用能力的配置项及装配规则。
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
     * 执行 智能体 Web Mvc 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param auditService 用于完成本次业务处理的 auditService 参数。
     * @param clientIpResolver 用于完成本次业务处理的 clientIpResolver 参数。
     */
    public AgentWebMvcConfig(AuditService auditService, ClientIpResolver clientIpResolver) {
        this.auditService = auditService;
        this.clientIpResolver = clientIpResolver;
    }

    /**
     * 创建或保存 智能体 Web Mvc 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param registry 用于完成本次业务处理的 registry 参数。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuditInterceptor(auditService, clientIpResolver))
                .addPathPatterns("/api/**");
    }
}
