package com.shiyu.ai.web.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import com.shiyu.ai.web.interceptor.UserContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置
 *
 * 1. UserContextInterceptor — 填充 UserContextHolder 登录上下文（@Order(1) 优先执行）
 * 2. SaInterceptor           — Sa-Token 注解鉴权（@SaCheckPermission 等）
 */
@Configuration
@Order(1)
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
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/webjars/**",
                    "/h2/**",
                    "/education-resources/**"
                );
        // Sa-Token 拦截器，开启注解式鉴权功能
        // 默认构造函数 isAnnotation = true，自动扫描 @SaCheckPermission 等注解
        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
    }
}
