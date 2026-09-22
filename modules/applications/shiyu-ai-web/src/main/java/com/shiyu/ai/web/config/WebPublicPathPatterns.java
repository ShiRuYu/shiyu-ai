package com.shiyu.ai.web.config;

import com.shiyu.ai.common.web.config.WebPublicPathContributor;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 汇总无需登录的 Web 路径，供 Servlet 认证过滤器和 MVC 用户上下文拦截器共同使用。
 *
 * <p>公开路径只在这里声明一次；业务模块需要追加公开资源时，应通过
 * {@link WebPublicPathContributor} 提供路径，而不是在 Web 配置中复制白名单。
 */
final class WebPublicPathPatterns {

    /** 应用内建的认证、文档和开发工具公开路径。 */
    private static final List<String> DEFAULTS =
            List.of(
                    "/api/iam/auth/login",
                    "/api/iam/auth/register",
                    "/api/iam/auth/code-login",
                    "/api/iam/auth/forget-password",
                    "/api/iam/auth/refresh",
                    "/api/iam/auth/captcha/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/webjars/**",
                    "/h2/**");

    private WebPublicPathPatterns() {}

    /**
     * 合并应用内建路径和各业务模块贡献的公开路径，并按声明顺序去重。
     *
     * @param contributors 业务模块公开路径贡献者；为空时仅返回应用内建路径。
     * @return 供所有认证入口使用的不可变公开路径集合。
     */
    static List<String> all(List<WebPublicPathContributor> contributors) {
        Stream<String> contributed =
                contributors == null
                        ? Stream.empty()
                        : contributors.stream()
                                .filter(Objects::nonNull)
                                .map(WebPublicPathContributor::publicPathPatterns)
                                .filter(Objects::nonNull)
                                .flatMap(Collection::stream)
                                .filter(Objects::nonNull);
        return Stream.concat(DEFAULTS.stream(), contributed).distinct().toList();
    }
}
