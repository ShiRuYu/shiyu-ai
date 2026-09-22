package com.shiyu.ai.web.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.strategy.SaStrategy;
import cn.dev33.satoken.util.SaFoxUtil;

import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.foundation.enums.BizResultCode;
import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.common.web.config.WebPublicPathContributor;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * 定义 Sa Token 基础设施或应用能力的配置项及装配规则。
 */
@Configuration
public class SaTokenConfig {

    /**
     * publicPathContributors 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final List<WebPublicPathContributor> publicPathContributors;

    /**
     * 执行 Sa Token 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param publicPathContributors 用于完成本次业务处理的 publicPathContributors 参数。
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
     * <p>排除路径由 {@link WebPublicPathPatterns} 统一汇总，Servlet 过滤器与 MVC 拦截器共享同一份配置。
     */
    @Bean
    public SaServletFilter saServletFilter() {
        SaServletFilter filter =
                new SaServletFilter()
                .addInclude("/**")
                .addExclude(
                        WebPublicPathPatterns.all(publicPathContributors)
                                .toArray(String[]::new));
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
