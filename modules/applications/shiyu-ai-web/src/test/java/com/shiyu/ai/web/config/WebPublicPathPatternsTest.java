package com.shiyu.ai.web.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import com.shiyu.ai.common.web.config.WebPublicPathContributor;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** 验证认证过滤器与用户上下文拦截器共享同一份公开路径集合。 */
@Tag("dev")
class WebPublicPathPatternsTest {

    @Test
    void returnsBuiltInPublicPathsInStableOrder() {
        assertIterableEquals(
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
                        "/h2/**"),
                WebPublicPathPatterns.all(List.of()));
    }

    @Test
    void mergesContributorsAndRemovesDuplicatePatterns() {
        WebPublicPathContributor first =
                () -> List.of("/education/resources/**", "/api/iam/auth/login");
        WebPublicPathContributor second =
                () -> List.of("/education/resources/**", "/education/public/**");

        List<String> paths = WebPublicPathPatterns.all(Arrays.asList(first, null, second));

        assertEquals(12, paths.size());
        assertEquals(1, paths.stream().filter("/education/resources/**"::equals).count());
        assertEquals("/education/resources/**", paths.get(10));
        assertEquals("/education/public/**", paths.get(11));
    }

    @Test
    void ignoresNullContributorCollectionsAndAcceptsNullContributorList() {
        WebPublicPathContributor nullCollection = () -> null;

        assertEquals(10, WebPublicPathPatterns.all(List.of(nullCollection)).size());
        assertEquals(10, WebPublicPathPatterns.all(null).size());
    }
}
