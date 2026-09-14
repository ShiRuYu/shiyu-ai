package com.shiyu.ai.common.storage.idempotency;

import com.shiyu.ai.common.storage.api.IdempotencyStore;
import com.shiyu.ai.common.storage.config.RedisInfrastructureProperties;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

/**
 * 使用 Redis 保存跨实例幂等键及其处理结果。
 */
public final class RedisIdempotencyStore implements IdempotencyStore {

    /**
     * Redis，表示当前对象中的对应属性。
     */
    private final StringRedisTemplate redis;
    /**
     * 配置属性，表示当前对象中的对应属性。
     */
    private final RedisInfrastructureProperties properties;

    /**
     * {@code RedisIdempotencyStore} 创建并初始化当前类型实例。
     *
     * @param redis 参数值，用于执行当前操作。
     * @param properties 参数值，用于执行当前操作。
     */
    public RedisIdempotencyStore(
            StringRedisTemplate redis, RedisInfrastructureProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    /**
     * {@code putIfAbsent} 执行当前类型定义的业务操作。
     *
     * @param key 参数值，用于执行当前操作。
     * @param ttl 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public boolean putIfAbsent(String key, Duration ttl) {
        if (key == null || key.isBlank() || ttl == null || ttl.isNegative() || ttl.isZero())
            return false;
        return Boolean.TRUE.equals(
                redis.opsForValue().setIfAbsent(properties.key("idempotency", key), "1", ttl));
    }

    /**
     * {@code contains} 执行当前类型定义的业务操作。
     *
     * @param key 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public boolean contains(String key) {
        return key != null
                && !key.isBlank()
                && Boolean.TRUE.equals(redis.hasKey(properties.key("idempotency", key)));
    }
}
