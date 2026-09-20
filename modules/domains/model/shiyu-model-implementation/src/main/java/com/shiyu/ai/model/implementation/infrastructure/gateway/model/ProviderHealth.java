package com.shiyu.ai.model.implementation.infrastructure.gateway.model;

import java.time.Instant;

/**
 * 封装 Provider Health 相关的不可变数据及其字段约束。
 */
public record ProviderHealth(
        String provider,
        String model,
        boolean healthy,
        int consecutiveFailures,
        Instant checkedAt,
        String message) {}
