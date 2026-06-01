package com.shiyu.ai.agent.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.shiyu.ai.agent.interceptor.UserContextInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 拦截器配置
 * 注册拦截器并配置白名单路径
 */
@Configuration
public class SaInterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private UserContextInterceptor userContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，打开注解鉴权功能
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter
                .match("/**")
                .notMatch("/auth/login")
                .notMatch("/auth/captcha")
                .notMatch("/auth/captcha/validate")
                .notMatch("/doc.html")
                .notMatch("/swagger-ui/**")
                .notMatch("/v3/api-docs/**")
                .notMatch("/webjars/**")
                .notMatch("/v2/api-docs")
                .notMatch("/h2/**")
                .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");

        // 注册用户上下文拦截器（将登录信息填充到 UserGlobalContext）
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/auth/login",
                    "/auth/captcha",
                    "/auth/captcha/validate",
                    "/doc.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/webjars/**", "/v2/api-docs",
                    "/h2/**"
                );
    }
}
