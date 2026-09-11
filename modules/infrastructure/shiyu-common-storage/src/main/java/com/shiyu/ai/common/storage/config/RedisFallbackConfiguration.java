package com.shiyu.ai.common.storage.config;

import com.shiyu.ai.common.storage.api.IdempotencyStore;
import com.shiyu.ai.common.storage.idempotency.LocalIdempotencyStore;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Exposes the disabled-provider implementations through the provider ports. */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        prefix = "shiyu.infrastructure.redis",
        name = "provider",
        havingValue = "disabled",
        matchIfMissing = true)
public class RedisFallbackConfiguration {

    @Bean
    public IdempotencyStore localIdempotencyStore() {
        return new LocalIdempotencyStore();
    }
}
