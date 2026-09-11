package com.shiyu.ai.model.implementation.infrastructure.gateway;

import java.time.Instant;

public record ProviderHealth(
        String provider,
        String model,
        boolean healthy,
        int consecutiveFailures,
        Instant checkedAt,
        String message) {}
