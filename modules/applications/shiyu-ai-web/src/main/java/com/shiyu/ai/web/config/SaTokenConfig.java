package com.shiyu.ai.web.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.strategy.SaStrategy;
import cn.dev33.satoken.util.SaFoxUtil;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.enums.BizResultCode;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.common.web.config.WebPublicPathContributor;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * {@code SaTokenConfig} 提供Web模块的配置项，并集中声明其默认值和运行约束。
 */
@Configuration
public class SaTokenConfig {

    /**
     * publicPathContributors 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final List<WebPublicPathContributor> publicPathContributors;

    /**
     * {@code SaTokenConfig} 创建并初始化当前类型实例。
     *
     * @param publicPathContributors 参数值，用于执行当前操作。
     */
    public SaTokenConfig(List<WebPublicPathContributor> publicPathContributors) {
        this.publicPathContributors = publicPathContributors;
    }

    /**
     * 重写 Sa-Token 框架内部算法策略
     *
     * <p>格式：Base64(userId)_{random50} - 前缀是 userId 的 Base64 编码（可逆），不含原始 userId 明文 - 服务器重启后仍能从 token
     * 字符串中恢复 userId - 后缀是 50 位随机字符串，保证 token 不可预测
     */
    @PostConstruct
    public void rewriteSaStrategy() {
        SaStrategy.instance.createToken =
                (loginId, loginType) -> {
                    String encoded =
                            Base64.getUrlEncoder()
                                    .withoutPadding()
                                    .encodeToString(
                                            loginId.toString().getBytes(StandardCharsets.UTF_8));
                    return encoded + "_" + SaFoxUtil.getRandomString(50);
                };
    }

    /**
     * Sa-Token 全局过滤器（Servlet 版） 替代 SaInterceptor 的路由拦截方式，对异步派发更友好
     *
     * <p>注意：排除路径需要与 com.shiyu.ai.web.config.SaInterceptorConfig 保持一致
     */
    @Bean
    public SaServletFilter saServletFilter() {
        SaServletFilter filter =
                new SaServletFilter()
                .addInclude("/**")
                // 认证相关公开接口（无需登录即可访问）
                .addExclude(
                        "/api/iam/auth/login",
                        "/api/iam/auth/register",
                        "/api/iam/auth/code-login",
                        "/api/iam/auth/forget-password",
                        "/api/iam/auth/refresh",
                        "/api/iam/auth/captcha/**")
                // 文档和监控接口
                .addExclude("/swagger-ui/**", "/v3/api-docs/**")
                .addExclude("/webjars/**", "/h2/**");
        publicPathContributors.stream()
                .map(WebPublicPathContributor::publicPathPatterns)
                .filter(java.util.Objects::nonNull)
                .flatMap(java.util.Collection::stream)
                .forEach(filter::addExclude);
        return filter
                .setAuth(
                        obj -> {
                            // 鉴权：检查是否登录
                            SaRouter.match("/**").check(r -> StpUtil.checkLogin());
                        })
                .setError(
                        e -> {
                            BizResultCode resultCode = BizResultCode.BAD_REQUEST;
                            if (e instanceof NotLoginException) {
                                resultCode = BizResultCode.UNAUTHORIZED;
                            }
                            return JSONUtils.toJsonString(Result.fail(resultCode));
                        });
    }
}
