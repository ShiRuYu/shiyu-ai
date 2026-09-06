package com.shiyu.ai.common.web.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/** HTTP adapter for resolving the originating client address. */
@Component
public class HttpClientIpResolver implements ClientIpResolver {

    private final HttpServletRequest request;

    public HttpClientIpResolver(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String currentClientIp() {
        String ip = firstUsable(
                request.getHeader("X-Forwarded-For"),
                request.getHeader("Proxy-Client-IP"),
                request.getHeader("WL-Proxy-Client-IP"),
                request.getRemoteAddr());
        return ip == null ? "unknown" : firstForwardedAddress(ip);
    }

    private static String firstUsable(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank() && !"unknown".equalsIgnoreCase(candidate.trim())) {
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
