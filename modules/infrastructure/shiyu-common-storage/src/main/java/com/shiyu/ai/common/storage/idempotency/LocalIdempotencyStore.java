package com.shiyu.ai.common.storage.idempotency;

import com.shiyu.ai.common.storage.api.IdempotencyStore;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/** In-process fallback for local development and single-node operation. */
public final class LocalIdempotencyStore implements IdempotencyStore {

    private final ConcurrentHashMap<String, Long> keys = new ConcurrentHashMap<>();

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
