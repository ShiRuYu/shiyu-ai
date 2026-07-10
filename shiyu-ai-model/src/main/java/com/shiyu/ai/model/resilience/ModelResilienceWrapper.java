package com.shiyu.ai.model.resilience;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * 模型调用弹性包装器
 * 组合 熔断器 + 限流器 + 降级策略
 */
@Slf4j
public class ModelResilienceWrapper {

    private final CircuitBreaker circuitBreaker;
    private final RateLimiter rateLimiter;
    private final String platformName;

    public ModelResilienceWrapper(String platformName) {
        this(platformName, new CircuitBreaker(), new RateLimiter());
    }

    public ModelResilienceWrapper(String platformName, CircuitBreaker circuitBreaker, RateLimiter rateLimiter) {
        this.platformName = platformName;
        this.circuitBreaker = circuitBreaker;
        this.rateLimiter = rateLimiter;
    }

    /**
     * 执行带弹性策略的调用
     * @param chatCall 模型调用
     * @param fallback 降级调用
     * @param <T> 返回类型
     * @return 调用结果
     */
    public <T> T execute(Supplier<T> chatCall, Supplier<T> fallback) {
        // 1. 检查熔断器
        if (!circuitBreaker.isAllowed()) {
            log.warn("[{}] 熔断器已打开，执行降级", platformName);
            if (fallback != null) {
                return fallback.get();
            }
            throw new RuntimeException("[" + platformName + "] 熔断器打开，且无降级方案");
        }

        // 2. 检查限流器
        if (!rateLimiter.tryAcquire()) {
            log.warn("[{}] 被限流，执行降级", platformName);
            if (fallback != null) {
                return fallback.get();
            }
            throw new RuntimeException("[" + platformName + "] 被限流，且无降级方案");
        }

        // 3. 调用模型
        return FallbackStrategy.executeWithFallback(() -> {
            try {
                T result = chatCall.get();
                circuitBreaker.onSuccess();
                return result;
            } catch (Exception e) {
                circuitBreaker.onFailure();
                throw e;
            }
        }, fallback);
    }

    public CircuitBreaker getCircuitBreaker() { return circuitBreaker; }
    public RateLimiter getRateLimiter() { return rateLimiter; }
}
