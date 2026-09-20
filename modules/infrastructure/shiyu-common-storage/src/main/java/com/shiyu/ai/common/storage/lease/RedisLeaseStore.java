package com.shiyu.ai.common.storage.lease;

import com.shiyu.ai.common.storage.api.DistributedLeaseStore;
import com.shiyu.ai.common.storage.config.RedisInfrastructureProperties;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.List;

/**
 * 管理 Redis Lease 相关的运行时状态、注册信息或临时数据。
 */
public final class RedisLeaseStore implements DistributedLeaseStore {

    private static final DefaultRedisScript<Long> ACQUIRE =
            new DefaultRedisScript<>(
                    """
                    if redis.call('exists', KEYS[1]) == 0 then
                      redis.call('psetex', KEYS[1], ARGV[2], ARGV[1])
                      return 1
                    end
                    if redis.call('get', KEYS[1]) == ARGV[1] then
                      redis.call('pexpire', KEYS[1], ARGV[2])
                      return 1
                    end
                    return 0
                    """,
                    Long.class);

    private static final DefaultRedisScript<Long> RENEW =
            new DefaultRedisScript<>(
                    """
                    if redis.call('get', KEYS[1]) == ARGV[1] then
                      redis.call('pexpire', KEYS[1], ARGV[2])
                      return 1
                    end
                    return 0
                    """,
                    Long.class);

    private static final DefaultRedisScript<Long> RELEASE =
            new DefaultRedisScript<>(
                    """
                    if redis.call('get', KEYS[1]) == ARGV[1] then
                      return redis.call('del', KEYS[1])
                    end
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
     * 执行 Redis Lease 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param redis 用于完成本次业务处理的 redis 参数。
     * @param properties 用于完成本次业务处理的 properties 参数。
     */
    public RedisLeaseStore(StringRedisTemplate redis, RedisInfrastructureProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    /**
     * 执行 Redis Lease 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @param ttl 用于完成本次业务处理的 ttl 参数。
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean tryAcquire(String key, String owner, Duration ttl) {
        return execute(ACQUIRE, key, owner, ttl) == 1L;
    }

    /**
     * 执行 Redis Lease 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @param ttl 用于完成本次业务处理的 ttl 参数。
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean renew(String key, String owner, Duration ttl) {
        return execute(RENEW, key, owner, ttl) == 1L;
    }

    /**
     * 执行 Redis Lease 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param owner 用于完成本次业务处理的 owner 参数。
     */
    @Override
    public void release(String key, String owner) {
        execute(RELEASE, key, owner, Duration.ZERO);
    }

    private long execute(DefaultRedisScript<Long> script, String key, String owner, Duration ttl) {
        if (key == null || key.isBlank() || owner == null || owner.isBlank()) return 0L;
        long millis = Math.max(1L, ttl.toMillis());
        Long result =
                redis.execute(
                        script,
                        List.of(properties.key("lease", key)),
                        owner,
                        String.valueOf(millis));
        return result == null ? 0L : result;
    }
}
