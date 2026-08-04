package com.shiyu.ai.web.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.strategy.SaStrategy;
import cn.dev33.satoken.util.SaFoxUtil;
import cn.dev33.satoken.router.SaRouter;
import com.shiyu.ai.common.core.enums.BizResultCode;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.common.core.api.Result;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;
import java.nio.charset.StandardCharsets;

@Configuration
public class SaTokenConfig {

    /**
     * 重写 Sa-Token 框架内部算法策略
     *
     * 格式：Base64(userId)_{random50}
     * - 前缀是 userId 的 Base64 编码（可逆），不含原始 userId 明文
     * - 服务器重启后仍能从 token 字符串中恢复 userId
     * - 后缀是 50 位随机字符串，保证 token 不可预测
     */
    @PostConstruct
    public void rewriteSaStrategy() {
        SaStrategy.instance.createToken = (loginId, loginType) -> {
            String encoded = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(loginId.toString().getBytes(StandardCharsets.UTF_8));
            return encoded + "_" + SaFoxUtil.getRandomString(50);
        };
    }

    /**
     * Sa-Token 全局过滤器（Servlet 版）
     * 替代 SaInterceptor 的路由拦截方式，对异步派发更友好
     *
     * 注意：排除路径需要与 com.shiyu.ai.web.config.SaInterceptorConfig 保持一致
     */
    @Bean
    public SaServletFilter saServletFilter() {
        return new SaServletFilter()
                .addInclude("/**")
                // 认证相关公开接口（无需登录即可访问）
                .addExclude(
                    "/auth/login",
                    "/auth/register",
                    "/auth/code-login",
                    "/auth/forget-password",
                    "/captcha/**",
                    "/auth/captcha"
                )
                // 文档和监控接口
                .addExclude("/swagger-ui/**", "/v3/api-docs/**")
                .addExclude(
                    "/webjars/**",
                    "/h2/**",
                    "/education-resources/**"
                )
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
