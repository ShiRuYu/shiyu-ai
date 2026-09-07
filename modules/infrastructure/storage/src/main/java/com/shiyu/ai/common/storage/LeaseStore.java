package com.shiyu.ai.common.storage;

import java.time.Duration;

/** Replaceable lease boundary. Local implementations may use an in-process lock; P3 can bind Redis/SQL leases. */
public interface LeaseStore {
    boolean tryAcquire(String key, String owner, Duration ttl);
    boolean renew(String key, String owner, Duration ttl);
    void release(String key, String owner);
}
