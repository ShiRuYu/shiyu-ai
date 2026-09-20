package com.shiyu.ai.common.storage.rate;

import com.shiyu.ai.common.storage.api.RateLimitStore;
import com.shiyu.ai.common.storage.config.RedisInfrastructureProperties;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.List;

/**
 * 管理 Redis Rate Limit 相关的运行时状态、注册信息或临时数据。
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
     * 执行 Redis Rate Limit 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param redis 用于完成本次业务处理的 redis 参数。
     * @param properties 用于完成本次业务处理的 properties 参数。
     */
    public RedisRateLimitStore(
            StringRedisTemplate redis, RedisInfrastructureProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    /**
     * 处理 Redis Rate Limit 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param permits 用于完成本次业务处理的 permits 参数。
     * @param limit 每页返回的数据数量。
     * @param window 用于完成本次业务处理的 window 参数。
     * @return 返回本次条件判断是否成立。
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
