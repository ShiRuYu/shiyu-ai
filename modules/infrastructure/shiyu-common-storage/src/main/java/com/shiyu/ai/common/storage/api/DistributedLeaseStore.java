package com.shiyu.ai.common.storage.api;

import java.time.Duration;

/** Provider-neutral lease API for short-lived distributed coordination. */
public interface DistributedLeaseStore extends LeaseStore {

    default boolean acquire(String key, Duration ttl) {
        return tryAcquire(key, owner(key), ttl);
    }

    default boolean renew(String key, Duration ttl) {
        return renew(key, owner(key), ttl);
    }

    default void release(String key) {
        release(key, owner(key));
    }

    private String owner(String key) {
        return getClass().getName()
                + "@"
                + System.identityHashCode(this)
                + ":thread-"
                + Thread.currentThread().threadId()
                + ":"
                + key;
    }
}
