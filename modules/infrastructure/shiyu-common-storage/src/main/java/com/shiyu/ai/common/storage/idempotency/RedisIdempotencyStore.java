package com.shiyu.ai.common.storage.idempotency;

import com.shiyu.ai.common.storage.api.IdempotencyStore;
import com.shiyu.ai.common.storage.config.RedisInfrastructureProperties;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

/** Redis-backed idempotency keys with atomic SET NX and expiry. */
public final class RedisIdempotencyStore implements IdempotencyStore {

    private final StringRedisTemplate redis;
    private final RedisInfrastructureProperties properties;

    public RedisIdempotencyStore(
            StringRedisTemplate redis, RedisInfrastructureProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    @Override
    public boolean putIfAbsent(String key, Duration ttl) {
        if (key == null || key.isBlank() || ttl == null || ttl.isNegative() || ttl.isZero())
            return false;
        return Boolean.TRUE.equals(
                redis.opsForValue().setIfAbsent(properties.key("idempotency", key), "1", ttl));
    }

    @Override
    public boolean contains(String key) {
        return key != null
                && !key.isBlank()
                && Boolean.TRUE.equals(redis.hasKey(properties.key("idempotency", key)));
    }
}
