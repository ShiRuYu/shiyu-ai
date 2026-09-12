package com.shiyu.ai.common.storage.idempotency;

import com.shiyu.ai.common.storage.api.IdempotencyStore;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用本地内存保存幂等键及其处理结果。
 */
public final class LocalIdempotencyStore implements IdempotencyStore {

    private final ConcurrentHashMap<String, Long> keys = new ConcurrentHashMap<>();

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
     * {@code contains} 执行当前类型定义的业务操作。
     *
     * @param key 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
