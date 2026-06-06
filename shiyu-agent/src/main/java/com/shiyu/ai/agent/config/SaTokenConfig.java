package com.shiyu.ai.agent.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.router.SaRouter;
import com.shiyu.ai.common.core.enums.BizResultCode;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.common.core.api.Result;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 配置类
 * 配置 JWT 模式、路由鉴权过滤器等
 */
@Configuration
public class SaTokenConfig {

    /**
     * Sa-Token 整合 JWT (简单模式)
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

    /**
     * Sa-Token 全局过滤器（Servlet 版）
     * 替代 SaInterceptor 的路由拦截方式，对异步派发更友好
     */
    @Bean
    public SaServletFilter saServletFilter() {
        return new SaServletFilter()
                .addInclude("/**")
                .addExclude("/auth/login", "/auth/captcha", "/auth/captcha/validate")
                .addExclude("/doc.html", "/swagger-ui/**", "/v3/api-docs/**")
                .addExclude("/webjars/**", "/v2/api-docs", "/h2/**")
                .setAuth(obj -> {
                    // 鉴权：检查是否登录
                    SaRouter.match("/**").check(r -> StpUtil.checkLogin());
                })
                .setError(e -> {
                    BizResultCode resultCode = BizResultCode.BAD_REQUEST;
                    if (e instanceof NotLoginException) {
                        resultCode = BizResultCode.UNAUTHORIZED;
                    }
                    return JSONUtils.toJsonString(Result.fail(resultCode));
                });
    }
}
