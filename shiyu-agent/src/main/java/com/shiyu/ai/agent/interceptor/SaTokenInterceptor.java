package com.shiyu.ai.agent.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Sa-Token 拦截器
 * 验证 Token 有效性，自动处理登录状态检查
 * 
 * 注意：此拦截器负责验证 Token，UserContextInterceptor 负责将用户信息填充到 UserGlobalContext
 * 业务代码应通过 LoginHelper 获取用户信息，而不是直接使用 StpUtil
 */
@Slf4j
@Component
public class SaTokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 请求直接放行（CORS 预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        try {
            // 验证登录状态（如果未登录会抛出 NotLoginException）
            StpUtil.checkLogin();
            
            // 日志记录（可选）
            if (log.isDebugEnabled()) {
                Long userId = StpUtil.getLoginIdAsLong();
                log.debug("用户已登录: userId={}, uri={}", userId, request.getRequestURI());
            }
            
            return true;
            
        } catch (Exception e) {
            log.warn("Token 验证失败: uri={}, error={}", request.getRequestURI(), e.getMessage());
            // 异常由全局异常处理器统一处理
            throw e;
        }
    }
}
