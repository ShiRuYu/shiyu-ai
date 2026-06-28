package com.shiyu.ai.bootstrap.config;

import com.shiyu.ai.bootstrap.interceptor.UserContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 鎷︽埅鍣ㄩ厤缃?
 * 娉ㄥ唽鐢ㄦ埛涓婁笅鏂囨嫤鎴櫒锛堥壌鏉冧娇鐢?SaServletFilter锛屽湪 SaTokenConfig 涓厤缃級
 */
@Configuration
public class SaInterceptorConfig implements WebMvcConfigurer {

    private final UserContextInterceptor userContextInterceptor;

    public SaInterceptorConfig(UserContextInterceptor userContextInterceptor) {
        this.userContextInterceptor = userContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 娉ㄥ唽鐢ㄦ埛涓婁笅鏂囨嫤鎴櫒锛堝皢鐧诲綍淇℃伅濉厖鍒?UserGlobalContext锛?
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
