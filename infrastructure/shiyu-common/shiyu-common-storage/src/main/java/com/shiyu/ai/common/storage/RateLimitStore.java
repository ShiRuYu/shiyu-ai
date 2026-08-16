package com.shiyu.ai.common.storage;

import java.time.Duration;

/** Distributed rate-limit boundary; single-node deployments can provide a local implementation. */
public interface RateLimitStore {
    boolean consume(String key, long permits, long limit, Duration window);
}
