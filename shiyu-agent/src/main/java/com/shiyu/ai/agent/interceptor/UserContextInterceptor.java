package com.shiyu.ai.agent.interceptor;

import com.shiyu.ai.agent.utils.SaTokenHelper;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.domain.LoginUser;
import com.shiyu.ai.common.core.enums.DeviceTypeEnum;
import com.shiyu.ai.common.core.enums.UserTypeEnum;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户上下文拦截器
 * 将 Sa-Token 的登录信息填充到 UserGlobalContext 中
 * 使得后续业务逻辑可以通过 LoginContextHolder 获取当前登录用户信息
 */
@Slf4j
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // OPTIONS 请求直接放行（CORS 预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 异步派发（如 SSE）不重新设置上下文，避免覆盖控制器线程已设置的值
        if (request.getDispatcherType() == DispatcherType.ASYNC) {
            return true;
        }

        try {
            // 检查是否已登录
            SaTokenHelper helper = SaTokenHelper.getInstance();
            if (!helper.isFrameworkLogin()) {
                log.debug("用户未登录，跳过用户上下文设置");
                return true;
            }

            // 从 Sa-Token 获取用户 ID
            Long userId = SaTokenHelper.getCurrentUserId();
            
            // 构建 LoginUser 对象
            LoginUser loginUser = new LoginUser();
            loginUser.setUserId(userId);
            loginUser.setToken(SaTokenHelper.getCurrentToken());
            loginUser.setUserType(UserTypeEnum.SYS_USER); // 默认系统用户类型
            loginUser.setLoginTime(System.currentTimeMillis());
            loginUser.setExpireTime(helper.getTokenTimeout());
            loginUser.setIpaddr(getClientIp(request));
            loginUser.setUsername(String.valueOf(userId)); // 使用 userId 作为 username
            
            // 设置设备信息（可以从 User-Agent 解析）
            String userAgent = request.getHeader("User-Agent");
            if (userAgent != null) {
                loginUser.setBrowser(parseBrowser(userAgent));
                loginUser.setOs(parseOS(userAgent));
            }
            
            // 将 LoginUser 设置到 UserGlobalContext
            LoginContextHolder.setContext(loginUser);
            
            log.debug("用户上下文设置成功: userId={}, uri={}", userId, request.getRequestURI());
            
            return true;
            
        } catch (Exception e) {
            log.warn("设置用户上下文失败: uri={}, error={}", request.getRequestURI(), e.getMessage());
            // 不抛出异常，避免影响正常业务流程
            return true;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完成后清理用户上下文，防止内存泄漏
        LoginContextHolder.clearContext();
        log.debug("用户上下文已清理: uri={}", request.getRequestURI());
    }

    /**
     * 获取客户端 IP 地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于通过多个代理的情况，第一个 IP 为客户端真实 IP，多个 IP 按照 ',' 分割
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        return ip;
    }

    /**
     * 解析浏览器类型
     */
    private String parseBrowser(String userAgent) {
        if (userAgent.contains("Chrome")) {
            return "Chrome";
        } else if (userAgent.contains("Firefox")) {
            return "Firefox";
        } else if (userAgent.contains("Safari")) {
            return "Safari";
        } else if (userAgent.contains("Edge")) {
            return "Edge";
        } else if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
            return "IE";
        }
        return "Unknown";
    }

    /**
     * 解析操作系统类型
     */
    private DeviceTypeEnum parseOS(String userAgent) {
        if (userAgent.contains("Windows")) {
            return DeviceTypeEnum.WINDOWS;
        } else if (userAgent.contains("Mac OS")) {
            return DeviceTypeEnum.MAC;
        } else if (userAgent.contains("Linux")) {
            return DeviceTypeEnum.LINUX;
        } else if (userAgent.contains("Android")) {
            return DeviceTypeEnum.ANDROID;
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            return DeviceTypeEnum.IOS;
        }
        return DeviceTypeEnum.UNKNOWN;
    }
}
