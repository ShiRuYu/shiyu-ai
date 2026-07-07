package com.shiyu.ai.auth.handler;

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

    private final Map<String, RateLimitEntry> attempts = new ConcurrentHashMap<>();

    private final int maxAttempts = 5;
    private final long windowMs = 60_000;
    /** 超过限制后临时封禁时长（秒） */
    private static final long LOCK_DURATION_SECONDS = 60;

    public boolean isAllowed(String ip) {
        long now = System.currentTimeMillis();
        RateLimitEntry entry = attempts.computeIfAbsent(ip, k -> new RateLimitEntry());

        synchronized (entry) {
            // 检查是否在封禁中
            if (entry.lockedUntil > now) {
                log.warn("IP 已被临时封禁至 {}, IP: {}", new java.util.Date(entry.lockedUntil), ip);
                return false;
            }

            // 窗口已过期，重置
            if (entry.windowStart < now - windowMs) {
                entry.windowStart = now;
                entry.count = 1;
                return true;
            }

            entry.count++;
            if (entry.count > maxAttempts) {
                entry.lockedUntil = now + LOCK_DURATION_SECONDS * 1000;
                log.warn("登录频率超限，IP 已封禁 {} 秒: {}", LOCK_DURATION_SECONDS, ip);
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

    /**
     * 限流条目：窗口起始时间（ms）、计数、封禁截止时间（ms）
     */
    private static class RateLimitEntry {
        long windowStart;
        int count;
        long lockedUntil;
    }
}
