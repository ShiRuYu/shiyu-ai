package com.shiyu.ai.web.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import com.shiyu.ai.web.interceptor.UserContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置
 * 注册用户上下文拦截器（鉴权使用 SaServletFilter，在 SaTokenConfig 中配置）
 *
 * 注意：排除路径需要与 SaTokenConfig 保持一致
 */
@Configuration
public class SaInterceptorConfig implements WebMvcConfigurer {

    private final UserContextInterceptor userContextInterceptor;

    public SaInterceptorConfig(UserContextInterceptor userContextInterceptor) {
        this.userContextInterceptor = userContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册用户上下文拦截器（将登录信息填充到 UserGlobalContext）
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    // 认证相关公开接口（无需登录即可访问）
                    "/auth/login",
                    "/auth/register",
                    "/auth/code-login",
                    "/auth/forget-password",
                    "/captcha/**",
                    "/auth/captcha",
                    // 文档和监控接口
                    "/doc.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/webjars/**", "/v2/api-docs",
                    "/h2/**"
                );
        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
    }
}
