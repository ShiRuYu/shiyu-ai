package com.shiyu.ai.common.web.auth;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

/**
 * 从 HTTP 请求头和连接信息解析客户端 IP。
 */
@Component
public class HttpClientIpResolver implements ClientIpResolver {

    /**
     * 请求，表示当前对象中的对应属性。
     */
    private final HttpServletRequest request;

    /**
     * {@code HttpClientIpResolver} 创建并初始化当前类型实例。
     *
     * @param request 参数值，用于执行当前操作。
     */
    public HttpClientIpResolver(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * {@code currentClientIp} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public String currentClientIp() {
        String ip =
                firstUsable(
                        request.getHeader("X-Forwarded-For"),
                        request.getHeader("X-Real-IP"),
                        request.getHeader("Proxy-Client-IP"),
                        request.getHeader("WL-Proxy-Client-IP"),
                        request.getRemoteAddr());
        return ip == null ? "unknown" : firstForwardedAddress(ip);
    }

    private static String firstUsable(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null
                    && !candidate.isBlank()
                    && !"unknown".equalsIgnoreCase(candidate.trim())) {
                return candidate.trim();
            }
        }
        return null;
    }

    private static String firstForwardedAddress(String value) {
        int comma = value.indexOf(',');
        return comma < 0 ? value : value.substring(0, comma).trim();
    }
}
