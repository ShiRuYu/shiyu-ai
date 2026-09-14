package com.shiyu.ai.web.config;

import cn.dev33.satoken.interceptor.SaInterceptor;

import com.shiyu.ai.common.web.config.WebPublicPathContributor;
import com.shiyu.ai.web.interceptor.UserContextInterceptor;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 拦截器配置
 *
 * <p>1. UserContextInterceptor — 填充 UserContextHolder 登录上下文（@Order(1) 优先执行） 2. SaInterceptor —
 * Sa-Token 注解鉴权（@SaCheckPermission 等）
 */
@Configuration
@Order(1)
public class SaInterceptorConfig implements WebMvcConfigurer {

    /**
     * userContextInterceptor 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final UserContextInterceptor userContextInterceptor;
    /**
     * publicPathContributors 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final List<WebPublicPathContributor> publicPathContributors;

    /**
     * {@code SaInterceptorConfig} 创建并初始化当前类型实例。
     *
     * @param userContextInterceptor 参数值，用于执行当前操作。
     * @param publicPathContributors 参数值，用于执行当前操作。
     */
    public SaInterceptorConfig(
            UserContextInterceptor userContextInterceptor,
            List<WebPublicPathContributor> publicPathContributors) {
        this.userContextInterceptor = userContextInterceptor;
        this.publicPathContributors = publicPathContributors;
    }

    /**
     * {@code addInterceptors} 执行当前类型定义的业务操作。
     *
     * @param registry 参数值，用于执行当前操作。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册用户上下文拦截器（将登录信息填充到 UserGlobalContext）
        var userContextRegistration =
                registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 认证相关公开接口（无需登录即可访问）
                        "/api/iam/auth/login",
                        "/api/iam/auth/register",
                        "/api/iam/auth/code-login",
                        "/api/iam/auth/forget-password",
                        "/api/iam/auth/refresh",
                        "/api/iam/auth/captcha/**",
                        // 文档和监控接口
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/h2/**");
        publicPathContributors.stream()
                .map(WebPublicPathContributor::publicPathPatterns)
                .filter(java.util.Objects::nonNull)
                .flatMap(java.util.Collection::stream)
                .forEach(userContextRegistration::excludePathPatterns);
        // Sa-Token 拦截器，开启注解式鉴权功能
        // 默认构造函数 isAnnotation = true，自动扫描 @SaCheckPermission 等注解
        registry.addInterceptor(
                        new SaInterceptor() {
                            /**
                             * {@code preHandle} 执行当前类型定义的业务操作。
                             *
                             * @param request 参数值，用于执行当前操作。
                             * @param response 参数值，用于执行当前操作。
                             * @param handler 参数值，用于执行当前操作。
                             *
                             * @return 返回当前操作产生的结果。
                             */
                            @Override
                            public boolean preHandle(
                                    HttpServletRequest request,
                                    HttpServletResponse response,
                                    Object handler)
                                    throws Exception {
                                if (request.getDispatcherType() != DispatcherType.REQUEST) {
                                    return true;
                                }
                                return super.preHandle(request, response, handler);
                            }
                        })
                .addPathPatterns("/**");
    }
}
