package com.shiyu.ai.common.web.auth;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

/**
 * 根据请求上下文解析或路由 Http Client Ip 相关的处理能力。
 */
@Component
public class HttpClientIpResolver implements ClientIpResolver {

    /**
     * 请求，表示当前对象中的对应属性。
     */
    private final HttpServletRequest request;

    /**
     * 执行 Http Client Ip 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param request 封装本次操作所需业务字段的请求对象。
     */
    public HttpClientIpResolver(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * 执行 Http Client Ip 相关业务数据，并返回处理结果。
     *
     * @return 返回 Http Client Ip 相关操作生成的结果数据。
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
