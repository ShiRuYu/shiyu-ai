package com.shiyu.ai.model.resilience;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * 降级策略
 * 主调用失败时执行降级逻辑
 */
@Slf4j
public class FallbackStrategy {

    /**
     * 执行带降级的方法
     * @param primary 主调用
     * @param fallback 降级调用
     * @param <T> 返回类型
     * @return 主调用结果或降级结果
     */
    public static <T> T executeWithFallback(Supplier<T> primary, Supplier<T> fallback) {
        try {
            return primary.get();
        } catch (Exception e) {
            log.warn("主调用失败，执行降级: {}", e.getMessage());
            if (fallback != null) {
                return fallback.get();
            }
            throw new RuntimeException("主调用失败且无降级方案", e);
        }
    }
}
