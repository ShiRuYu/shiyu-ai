package com.shiyu.ai.auth.handler;

import com.shiyu.ai.common.web.auth.ClientIpResolver;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Objects;

/**
 * 登录速率限制器
 * 滑动窗口算法 + 随机抖动封禁时间 + 定期清理过期条目
 */
@Slf4j
@Component
public class LoginRateLimiter {

    private final Map<String, RateLimitEntry> attempts = new ConcurrentHashMap<>();

    /** 滑动窗口内最大尝试次数 */
    private final int maxAttempts = 5;
    /** 滑动窗口时间（毫秒） */
    private final long windowMs = 60_000;
    /** 基础封禁时长（秒） */
    private static final long BASE_LOCK_DURATION_SECONDS = 60;
    /** 随机抖动范围（秒）+/- 10秒 */
    private static final long JITTER_RANGE_SECONDS = 10;

    private final Random random = new Random();
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "login-rate-limiter-cleanup");
        t.setDaemon(true);
        return t;
    });

    private final ClientIpResolver clientIpResolver;

    /** Constructor used by the HTTP application context. */
    @Autowired
    public LoginRateLimiter(ClientIpResolver clientIpResolver) {
        this.clientIpResolver = Objects.requireNonNull(clientIpResolver, "clientIpResolver must not be null");
    }

    /** Constructor retained for pure unit tests that do not have an HTTP adapter. */
    public LoginRateLimiter() {
        this(() -> "unknown");
    }

    @PostConstruct
    public void init() {
        cleanupScheduler.scheduleAtFixedRate(this::cleanupExpiredEntries,
                5, 5, TimeUnit.MINUTES);
        log.info("登录限流器已初始化: 滑动窗口={}ms, 最大尝试={}, 封禁={}s±{}s",
                windowMs, maxAttempts, BASE_LOCK_DURATION_SECONDS, JITTER_RANGE_SECONDS);
    }

    @PreDestroy
    public void destroy() {
        cleanupScheduler.shutdownNow();
        try {
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("LoginRateLimiter 定时清理线程池未能正常关闭");
            }
        } catch (InterruptedException e) {
            cleanupScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("LoginRateLimiter 定时清理线程池已关闭");
    }

    public boolean isAllowed(String ip) {
        long now = System.currentTimeMillis();
        RateLimitEntry entry = attempts.computeIfAbsent(ip, k -> new RateLimitEntry());

        synchronized (entry) {
            // 检查是否在封禁中
            if (entry.lockedUntil > now) {
                log.warn("IP 已被临时封禁至 {}, IP: {}", new java.util.Date(entry.lockedUntil), ip);
                return false;
            }

            // 滑动窗口清理：移除窗口外的记录
            while (!entry.attempts.isEmpty() && entry.attempts.peek() < now - windowMs) {
                entry.attempts.poll();
            }

            entry.attempts.add(now);
            if (entry.attempts.size() > maxAttempts) {
                // 添加随机抖动封禁时间
                long jitter = (long) (random.nextDouble() * 2 * JITTER_RANGE_SECONDS * 1000 - JITTER_RANGE_SECONDS * 1000);
                entry.lockedUntil = now + BASE_LOCK_DURATION_SECONDS * 1000 + jitter;
                log.warn("登录频率超限，IP 已封禁 {}s±{}s: {}, 实际封禁={}ms",
                        BASE_LOCK_DURATION_SECONDS, JITTER_RANGE_SECONDS, ip,
                        BASE_LOCK_DURATION_SECONDS * 1000 + jitter);
                return false;
            }
            return true;
        }
    }

    public void reset(String ip) {
        attempts.remove(ip);
    }

    /**
     * 定期清理已过期的限流条目
     */
    private void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();
        int before = attempts.size();
        attempts.values().removeIf(entry -> {
            synchronized (entry) {
                // 封禁已过期的条目可以移除
                if (entry.lockedUntil > 0 && entry.lockedUntil < now) {
                    return true;
                }
                // 窗口内无记录的可以移除
                entry.attempts.removeIf(t -> t < now - windowMs);
                return entry.attempts.isEmpty() && entry.lockedUntil < now;
            }
        });
        int after = attempts.size();
        if (before != after) {
            log.debug("登录限流器清理过期条目: {} → {}", before, after);
        }
    }

    public String getClientIp() {
        return clientIpResolver.currentClientIp();
    }

    /**
     * 限流条目：使用 EvictingQueue 记录每次尝试时间实现真正的滑动窗口
     * 自动淘汰最旧记录，无需手动 removeIf
     */
    private static class RateLimitEntry {
        /** 尝试时间戳 EvictingQueue（自动淘汰最旧记录，容量=maxAttempts+1） */
        final java.util.Queue<Long> attempts = new java.util.concurrent.ConcurrentLinkedDeque<>();
        /** 封禁截止时间（毫秒） */
        long lockedUntil;
    }
}
