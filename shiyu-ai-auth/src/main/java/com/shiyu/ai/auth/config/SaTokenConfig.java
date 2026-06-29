package com.shiyu.ai.auth.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.strategy.SaStrategy;
import cn.dev33.satoken.util.SaFoxUtil;
import com.shiyu.ai.common.core.enums.BizResultCode;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.common.core.api.Result;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SaTokenConfig {
    /**
     * 閲嶅啓 Sa-Token 妗嗘灦鍐呴儴绠楁硶绛栫暐
     */
    @PostConstruct
    public void rewriteSaStrategy() {
        SaStrategy.instance.createToken = (loginId, loginType) ->
                loginId + "_" + SaFoxUtil.getRandomString(60);
    }

    /**
     * Sa-Token 鍏ㄥ眬杩囨护鍣紙Servlet 鐗堬級
     * 鏇夸唬 SaInterceptor 鐨勮矾鐢辨嫤鎴柟寮忥紝瀵瑰紓姝ユ淳鍙戞洿鍙嬪ソ
     */
    @Bean
    public SaServletFilter saServletFilter() {
        return new SaServletFilter()
                .addInclude("/**")
                .addExclude("/auth/login", "/auth/captcha", "/auth/captcha/validate")
                .addExclude("/doc.html", "/swagger-ui/**", "/v3/api-docs/**")
                .addExclude("/webjars/**", "/v2/api-docs", "/h2/**")
                .setAuth(obj -> {
                    // 閴存潈锛氭鏌ユ槸鍚︾櫥褰?                    SaRouter.match("/**").check(r -> StpUtil.checkLogin());
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
