package com.shiyu.ai.common.storage.rate;

import com.shiyu.ai.common.storage.api.RateLimitStore;
import com.shiyu.ai.common.storage.config.RedisInfrastructureProperties;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.List;

/** Redis-backed fixed-window rate limiter with an atomic increment/expiry script. */
public final class RedisRateLimitStore implements RateLimitStore {

    private static final DefaultRedisScript<Long> CONSUME =
            new DefaultRedisScript<>(
                    """
                    local current = redis.call('incrby', KEYS[1], ARGV[1])
                    if current == tonumber(ARGV[1]) then
                      redis.call('pexpire', KEYS[1], ARGV[3])
                    end
                    if current <= tonumber(ARGV[2]) then return 1 end
                    return 0
                    """,
                    Long.class);

    private final StringRedisTemplate redis;
    private final RedisInfrastructureProperties properties;

    public RedisRateLimitStore(
            StringRedisTemplate redis, RedisInfrastructureProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    @Override
    public boolean consume(String key, long permits, long limit, Duration window) {
        if (key == null
                || key.isBlank()
                || permits <= 0
                || limit <= 0
                || window.isZero()
                || window.isNegative()) {
            return false;
        }
        long windowMillis = Math.max(1L, window.toMillis());
        Long result =
                redis.execute(
                        CONSUME,
                        List.of(properties.key("rate", key)),
                        String.valueOf(permits),
                        String.valueOf(limit),
                        String.valueOf(windowMillis));
        return result != null && result == 1L;
    }
}
