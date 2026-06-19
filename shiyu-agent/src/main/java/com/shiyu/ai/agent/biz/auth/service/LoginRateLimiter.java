package com.shiyu.ai.agent.biz.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class LoginRateLimiter {

    private final Map<String, int[]> attempts = new ConcurrentHashMap<>();

    private final int maxAttempts = 5;
    private final long windowMs = 60000;

    public boolean isAllowed(String ip) {
        long now = System.currentTimeMillis();
        int[] timestamps = attempts.computeIfAbsent(ip, k -> new int[]{0, 0});
        synchronized (timestamps) {
            if (timestamps[0] < now - windowMs) {
                timestamps[0] = (int) now;
                timestamps[1] = 1;
                return true;
            }
            timestamps[1]++;
            if (timestamps[1] > maxAttempts) {
                log.warn("登录频率超限，IP: {}", ip);
                return false;
            }
            return true;
        }
    }

    public void reset(String ip) {
        attempts.remove(ip);
    }

    public String getClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return "unknown";
        HttpServletRequest request = attrs.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
