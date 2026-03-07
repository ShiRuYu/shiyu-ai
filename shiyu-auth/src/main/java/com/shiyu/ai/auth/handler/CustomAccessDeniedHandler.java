package com.shiyu.ai.auth.handler;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.enums.BizResultCode;
import com.shiyu.ai.common.core.utils.JSONUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 * Spring Security 访问拒绝处理器
 * 用于处理已认证但权限不足的情况
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, 
                       AccessDeniedException accessDeniedException) throws IOException {
        
        log.warn("访问被拒绝：{} - {}", request.getRequestURI(), accessDeniedException.getMessage());
        
        // 设置响应格式
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        // 返回统一的 Result 格式
        Result<String> result = Result.fail(BizResultCode.ERR_11003, "权限不足，无法访问此资源");
        response.getWriter().write(Objects.requireNonNull(JSONUtils.toJsonString(result)));
    }
}
