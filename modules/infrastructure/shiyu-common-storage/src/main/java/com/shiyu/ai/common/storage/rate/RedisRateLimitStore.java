package com.shiyu.ai.common.storage.rate;

import com.shiyu.ai.common.storage.api.RateLimitStore;
import com.shiyu.ai.common.storage.config.RedisInfrastructureProperties;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.List;

/**
 * 使用 Redis 保存并发限流计数和窗口状态。
 */
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

    /**
     * Redis，表示当前对象中的对应属性。
     */
    private final StringRedisTemplate redis;
    /**
     * 配置属性，表示当前对象中的对应属性。
     */
    private final RedisInfrastructureProperties properties;

    /**
     * {@code RedisRateLimitStore} 创建并初始化当前类型实例。
     *
     * @param redis 参数值，用于执行当前操作。
     * @param properties 参数值，用于执行当前操作。
     */
    public RedisRateLimitStore(
            StringRedisTemplate redis, RedisInfrastructureProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    /**
     * {@code consume} 执行当前类型定义的业务操作。
     *
     * @param key 参数值，用于执行当前操作。
     * @param permits 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     * @param window 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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
