package com.shiyu.ai.common.storage.lease;

import com.shiyu.ai.common.storage.api.DistributedLeaseStore;
import com.shiyu.ai.common.storage.config.RedisInfrastructureProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.List;

/** Redis implementation of the lease boundary using atomic Lua scripts. */
public final class RedisLeaseStore implements DistributedLeaseStore {

    private static final DefaultRedisScript<Long> ACQUIRE = new DefaultRedisScript<>("""
            if redis.call('exists', KEYS[1]) == 0 then
              redis.call('psetex', KEYS[1], ARGV[2], ARGV[1])
              return 1
            end
            if redis.call('get', KEYS[1]) == ARGV[1] then
              redis.call('pexpire', KEYS[1], ARGV[2])
              return 1
            end
            return 0
            """, Long.class);

    private static final DefaultRedisScript<Long> RENEW = new DefaultRedisScript<>("""
            if redis.call('get', KEYS[1]) == ARGV[1] then
              redis.call('pexpire', KEYS[1], ARGV[2])
              return 1
            end
            return 0
            """, Long.class);

    private static final DefaultRedisScript<Long> RELEASE = new DefaultRedisScript<>("""
            if redis.call('get', KEYS[1]) == ARGV[1] then
              return redis.call('del', KEYS[1])
            end
            return 0
            """, Long.class);

    private final StringRedisTemplate redis;
    private final RedisInfrastructureProperties properties;

    public RedisLeaseStore(StringRedisTemplate redis, RedisInfrastructureProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    @Override
    public boolean tryAcquire(String key, String owner, Duration ttl) {
        return execute(ACQUIRE, key, owner, ttl) == 1L;
    }

    @Override
    public boolean renew(String key, String owner, Duration ttl) {
        return execute(RENEW, key, owner, ttl) == 1L;
    }

    @Override
    public void release(String key, String owner) {
        execute(RELEASE, key, owner, Duration.ZERO);
    }

    private long execute(DefaultRedisScript<Long> script, String key, String owner, Duration ttl) {
        if (key == null || key.isBlank() || owner == null || owner.isBlank()) return 0L;
        long millis = Math.max(1L, ttl.toMillis());
        Long result = redis.execute(script,
                List.of(properties.key("lease", key)), owner, String.valueOf(millis));
        return result == null ? 0L : result;
    }
}
