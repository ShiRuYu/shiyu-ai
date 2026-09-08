package com.shiyu.ai.common.storage.rate;
import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LocalRateLimitStore implements RateLimitStore {
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    public boolean consume(String key, long permits, long limit, Duration window) {
        long now = System.nanoTime();
        Window next = windows.compute(key, (k, old) -> old == null || old.expiresAt < now ? new Window(now + window.toNanos(), permits) : new Window(old.expiresAt, old.used + permits));
        return next.used <= limit;
    }
    private record Window(long expiresAt, long used) { }
}
