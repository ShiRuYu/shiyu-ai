package com.shiyu.ai.web.config;

import com.shiyu.ai.common.foundation.context.UserContextHolder;

import com.shiyu.ai.common.foundation.context.UserGlobalContext;

import cn.dev33.satoken.interceptor.SaInterceptor;

import com.shiyu.ai.common.web.config.WebPublicPathContributor;
import com.shiyu.ai.web.interceptor.BusinessModuleAccessInterceptor;
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
 * 按用户上下文、租户模块访问和 Sa-Token 注解鉴权的顺序注册 MVC 拦截器。
 */
@Configuration
@Order(1)
public class SaInterceptorConfig implements WebMvcConfigurer {

    /**
     * 在认证请求中绑定当前用户与租户上下文，并在请求结束时清理上下文。
     */
    private final UserContextInterceptor userContextInterceptor;
    /**
     * 提供不需要经过用户上下文拦截器的公开路径。
     */
    private final List<WebPublicPathContributor> publicPathContributors;
    /** 根据当前租户的启用状态限制业务模块访问。 */
    private final BusinessModuleAccessInterceptor businessModuleAccessInterceptor;

    /**
     * 保存用户上下文拦截器、公开路径贡献者和租户模块访问拦截器，以便注册完整的 Web 授权链。
     *
     * @param userContextInterceptor 绑定并清理当前用户与租户上下文的拦截器。
     * @param publicPathContributors 声明公开访问路径及其用户上下文排除规则的组件集合。
     * @param businessModuleAccessInterceptor 检查当前租户是否启用目标业务模块的拦截器。
     */
    public SaInterceptorConfig(
            UserContextInterceptor userContextInterceptor,
            List<WebPublicPathContributor> publicPathContributors,
            BusinessModuleAccessInterceptor businessModuleAccessInterceptor) {
        this.userContextInterceptor = userContextInterceptor;
        this.publicPathContributors = publicPathContributors;
        this.businessModuleAccessInterceptor = businessModuleAccessInterceptor;
    }

    /**
     * 注册用户上下文、租户模块访问和 Sa-Token 注解鉴权拦截器，并排除公开路径的用户上下文绑定。
     *
     * @param registry Spring MVC 用于注册和配置拦截器的注册表。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册用户上下文拦截器（将登录信息填充到 UserGlobalContext）
        var userContextRegistration =
                registry.addInterceptor(userContextInterceptor)
                        .addPathPatterns("/**")
                        .excludePathPatterns(
                                WebPublicPathPatterns.all(publicPathContributors)
                                        .toArray(String[]::new));
        // 用户上下文已经绑定 TenantScope 后，先检查租户是否启用业务模块，再执行细粒度权限注解。
        registry.addInterceptor(businessModuleAccessInterceptor).addPathPatterns("/api/**");
        // Sa-Token 拦截器，开启注解式鉴权功能
        // 默认构造函数 isAnnotation = true，自动扫描 @SaCheckPermission 等注解
        registry.addInterceptor(
                        new SaInterceptor() {
                            /**
                             * 对普通 HTTP 请求执行 Sa-Token 注解鉴权，并放行其他分派类型。
                             *
                             * @param request 当前 HTTP 请求，用于判断请求分派类型。
                             * @param response 当前 HTTP 响应；此方法不直接写入响应内容。
                             * @param handler Spring MVC 为当前请求选定的控制器或处理器对象。
                             *
                             * @return 普通请求的注解鉴权结果；非普通请求分派返回 {@code true}。
                             * @throws Exception Sa-Token 执行注解鉴权时发生错误。
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
