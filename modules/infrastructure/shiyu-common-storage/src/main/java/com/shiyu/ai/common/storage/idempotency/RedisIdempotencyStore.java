package com.shiyu.ai.common.storage.idempotency;

import com.shiyu.ai.common.storage.api.IdempotencyStore;
import com.shiyu.ai.common.storage.config.RedisInfrastructureProperties;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

/**
 * 管理 Redis Idempotency 相关的运行时状态、注册信息或临时数据。
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
     * 执行 Redis Idempotency 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param redis 用于完成本次业务处理的 redis 参数。
     * @param properties 用于完成本次业务处理的 properties 参数。
     */
    public RedisIdempotencyStore(
            StringRedisTemplate redis, RedisInfrastructureProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    /**
     * 执行 Redis Idempotency 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param ttl 用于完成本次业务处理的 ttl 参数。
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean putIfAbsent(String key, Duration ttl) {
        if (key == null || key.isBlank() || ttl == null || ttl.isNegative() || ttl.isZero())
            return false;
        return Boolean.TRUE.equals(
                redis.opsForValue().setIfAbsent(properties.key("idempotency", key), "1", ttl));
    }

    /**
     * 执行 Redis Idempotency 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean contains(String key) {
        return key != null
                && !key.isBlank()
                && Boolean.TRUE.equals(redis.hasKey(properties.key("idempotency", key)));
    }
}
