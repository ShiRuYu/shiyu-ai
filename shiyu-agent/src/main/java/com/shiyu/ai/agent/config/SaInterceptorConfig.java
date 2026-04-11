package com.shiyu.ai.agent.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.shiyu.ai.agent.interceptor.SaTokenInterceptor;
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
    private SaTokenInterceptor saTokenInterceptor;

    @Autowired
    private UserContextInterceptor userContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，打开注解鉴权功能
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 指定一条 match 规则
            SaRouter
                .match("/**")    // 拦截所有路径
                // 排除不需要认证的路径
                .notMatch("/auth/login")           // 登录接口
                .notMatch("/auth/captcha")         // 验证码接口（保留但不使用）
                .notMatch("/auth/captcha/validate") // 验证码验证（保留但不使用）
                .notMatch("/doc.html")             // Knife4j 文档
                .notMatch("/swagger-ui/**")        // Swagger UI
                .notMatch("/v3/api-docs/**")       // OpenAPI 文档
                .notMatch("/webjars/**")           // Swagger 资源
                .notMatch("/h2/**")                // H2 控制台
                .check(r -> StpUtil.checkLogin()); // 检查登录状态
        })).addPathPatterns("/**");

        // 注册自定义拦截器（用于日志等额外功能）
        registry.addInterceptor(saTokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/auth/login",
                    "/auth/captcha",
                    "/auth/captcha/validate",
                    "/doc.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/webjars/**",
                    "/h2/**"
                );

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
                    "/webjars/**",
                    "/h2/**"
                );
    }
}
