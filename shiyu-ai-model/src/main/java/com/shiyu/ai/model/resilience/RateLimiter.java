package com.shiyu.ai.model.resilience;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 滑动窗口限流器
 */
@Slf4j
public class RateLimiter {

    private final int maxRequests;
    private final long windowMs;
    private final Deque<Long> timestamps = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock();

    public RateLimiter() {
        this(60, 60000); // 默认每分钟最多60次请求
    }

    public RateLimiter(int maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
    }

    /**
     * 尝试获取许可
     * @return true 允许通过，false 被限流
     */
    public boolean tryAcquire() {
        lock.lock();
        try {
            long now = System.currentTimeMillis();
            // 清理窗口外的时间戳
            while (!timestamps.isEmpty() && timestamps.peek() < now - windowMs) {
                timestamps.poll();
            }
            if (timestamps.size() >= maxRequests) {
                log.warn("限流触发: {}次/{}ms, 当前={}", maxRequests, windowMs, timestamps.size());
                return false;
            }
            timestamps.add(now);
            return true;
        } finally {
            lock.unlock();
        }
    }
}
