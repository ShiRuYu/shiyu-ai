package com.shiyu.ai.common.storage.idempotency;

import com.shiyu.ai.common.storage.api.IdempotencyStore;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理 Local Idempotency 相关的运行时状态、注册信息或临时数据。
 */
public final class LocalIdempotencyStore implements IdempotencyStore {

    private final ConcurrentHashMap<String, Long> keys = new ConcurrentHashMap<>();

    /**
     * 执行 Local Idempotency 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param ttl 用于完成本次业务处理的 ttl 参数。
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean putIfAbsent(String key, Duration ttl) {
        if (key == null || key.isBlank() || ttl == null || ttl.isNegative() || ttl.isZero())
            return false;
        long expiresAt = System.nanoTime() + ttl.toNanos();
        return keys.compute(
                        key,
                        (ignored, current) ->
                                current == null || current <= System.nanoTime()
                                        ? expiresAt
                                        : current)
                == expiresAt;
    }

    /**
     * 执行 Local Idempotency 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean contains(String key) {
        if (key == null || key.isBlank()) return false;
        Long expiresAt = keys.get(key);
        if (expiresAt == null) return false;
        if (expiresAt <= System.nanoTime()) {
            keys.remove(key, expiresAt);
            return false;
        }
        return true;
    }
}
