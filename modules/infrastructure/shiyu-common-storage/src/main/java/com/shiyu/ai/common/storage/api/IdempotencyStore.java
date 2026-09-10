package com.shiyu.ai.common.storage.api;

import java.time.Duration;

/** Short-lived idempotency key boundary. */
public interface IdempotencyStore {

    boolean putIfAbsent(String key, Duration ttl);

    boolean contains(String key);
}
