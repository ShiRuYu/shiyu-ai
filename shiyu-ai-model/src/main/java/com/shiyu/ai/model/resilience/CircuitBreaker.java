package com.shiyu.ai.model.resilience;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 熔断器
 * 状态：CLOSED(正常) → OPEN(熔断) → HALF_OPEN(半开尝试)
 */
@Slf4j
public class CircuitBreaker {

    public enum State { CLOSED, OPEN, HALF_OPEN }

    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final int failureThreshold;
    private final long timeoutMs;
    private volatile long lastFailureTime;

    public CircuitBreaker() {
        this(5, 30000); // 默认5次失败后熔断，30秒后尝试恢复
    }

    public CircuitBreaker(int failureThreshold, long timeoutMs) {
        this.failureThreshold = failureThreshold;
        this.timeoutMs = timeoutMs;
    }

    /**
     * 检查是否允许调用
     */
    public boolean isAllowed() {
        State current = state.get();
        if (current == State.CLOSED) return true;
        if (current == State.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime >= timeoutMs) {
                if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                    log.info("熔断器进入半开状态，允许试探请求");
                    return true;
                }
            }
            return false;
        }
        // HALF_OPEN — 允许
        return true;
    }

    /**
     * 记录成功调用
     */
    public void onSuccess() {
        State prev = state.getAndSet(State.CLOSED);
        if (prev != State.CLOSED) {
            log.info("熔断器关闭，恢复正常");
        }
        failureCount.set(0);
    }

    /**
     * 记录失败调用
     */
    public void onFailure() {
        lastFailureTime = System.currentTimeMillis();
        int failures = failureCount.incrementAndGet();
        if (failures >= failureThreshold) {
            if (state.compareAndSet(State.CLOSED, State.OPEN)) {
                log.warn("熔断器打开（失败次数: {}/{}），熔断 {}ms", failures, failureThreshold, timeoutMs);
            } else {
                state.set(State.OPEN);
            }
        }
    }

    public State getState() { return state.get(); }
    public int getFailureCount() { return failureCount.get(); }
}
