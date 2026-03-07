package com.shiyu.ai.auth.handler;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.enums.BizResultCode;
import com.shiyu.ai.common.core.utils.JSONUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 * Spring Security 认证失败处理器
 * 用于处理未登录或 Token 失效的情况
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                         AuthenticationException authException) throws IOException {
        
        log.warn("认证失败：{} - {}", request.getRequestURI(), authException.getMessage());
        
        // 设置响应格式
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        // 返回统一的 Result 格式
        Result<String> result = Result.fail(BizResultCode.ERR_10002, "未授权访问，请先登录");
        response.getWriter().write(Objects.requireNonNull(JSONUtils.toJsonString(result)));
    }
}
