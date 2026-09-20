package com.shiyu.ai.iam.implementation.handler;

import com.shiyu.ai.common.web.auth.ClientIpResolver;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 实现 Login Rate Limiter 相关的业务处理、协作逻辑或基础设施能力。
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
    private final ScheduledExecutorService cleanupScheduler =
            Executors.newSingleThreadScheduledExecutor(
                    r -> {
                        Thread t = new Thread(r, "login-rate-limiter-cleanup");
                        t.setDaemon(true);
                        return t;
                    });

    private final ClientIpResolver clientIpResolver;

    /**
     * 处理登录ratelimiter。
     *
     * @param clientIpResolver clientIpResolver 参数。
     *
     * @return 处理结果。
     */
    @Autowired
    public LoginRateLimiter(ClientIpResolver clientIpResolver) {
        this.clientIpResolver =
                Objects.requireNonNull(clientIpResolver, "clientIpResolver must not be null");
    }

    /**
     * 处理登录ratelimiter。
     *
     * @return 处理结果。
     */
    public LoginRateLimiter() {
        this(() -> "unknown");
    }

    /**
     * 执行 Login Rate Limiter 相关业务操作，并维护必要的状态和协作关系。
     */
    @PostConstruct
    public void init() {
        cleanupScheduler.scheduleAtFixedRate(this::cleanupExpiredEntries, 5, 5, TimeUnit.MINUTES);
        log.info(
                "登录限流器已初始化: 滑动窗口={}ms, 最大尝试={}, 封禁={}s±{}s",
                windowMs,
                maxAttempts,
                BASE_LOCK_DURATION_SECONDS,
                JITTER_RANGE_SECONDS);
    }

    /**
     * 执行 Login Rate Limiter 相关业务操作，并维护必要的状态和协作关系。
     */
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

    /**
     * 校验或判断 Login Rate Limiter 相关业务数据，并返回处理结果。
     *
     * @param ip 用于完成本次业务处理的 ip 参数。
     * @return 返回本次条件判断是否成立。
     */
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
                long jitter =
                        (long)
                                (random.nextDouble() * 2 * JITTER_RANGE_SECONDS * 1000
                                        - JITTER_RANGE_SECONDS * 1000);
                entry.lockedUntil = now + BASE_LOCK_DURATION_SECONDS * 1000 + jitter;
                log.warn(
                        "登录频率超限，IP 已封禁 {}s±{}s: {}, 实际封禁={}ms",
                        BASE_LOCK_DURATION_SECONDS,
                        JITTER_RANGE_SECONDS,
                        ip,
                        BASE_LOCK_DURATION_SECONDS * 1000 + jitter);
                return false;
            }
            return true;
        }
    }

    /**
     * 执行 Login Rate Limiter 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param ip 用于完成本次业务处理的 ip 参数。
     */
    public void reset(String ip) {
        attempts.remove(ip);
    }

    /** 定期清理已过期的限流条目 */
    private void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();
        int before = attempts.size();
        attempts.values()
                .removeIf(
                        entry -> {
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

    /**
     * 查询 Login Rate Limiter 相关业务数据，并返回处理结果。
     *
     * @return 返回 Login Rate Limiter 相关操作生成的结果数据。
     */
    public String getClientIp() {
        return clientIpResolver.currentClientIp();
    }

    /**
     * 表示 Rate Limit 相关流程中的状态、关系或执行数据。
     */
    private static class RateLimitEntry {
        /** 尝试时间戳 EvictingQueue（自动淘汰最旧记录，容量=maxAttempts+1） */
        final java.util.Queue<Long> attempts = new java.util.concurrent.ConcurrentLinkedDeque<>();

        /** 封禁截止时间（毫秒） */
        long lockedUntil;
    }
}
