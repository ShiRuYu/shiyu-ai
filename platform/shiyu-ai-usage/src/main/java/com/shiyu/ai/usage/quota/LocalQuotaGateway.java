package com.shiyu.ai.usage.quota;

import com.shiyu.ai.common.storage.LocalRateLimitStore;
import com.shiyu.ai.common.storage.RateLimitStore;
import com.shiyu.ai.usage.port.QuotaGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Single-instance admission/settlement gateway. P3 replaces this implementation with Redis-backed leases. */
@Service
public class LocalQuotaGateway implements QuotaGateway {
    private final long dailyTokenLimit;
    private final int defaultConcurrentLimit;
    private final Clock clock;
    private final RateLimitStore rateLimitStore;
    private final long requestsPerMinute;
    private final AtomicLong reservationSequence = new AtomicLong(1);
    private final Map<Long, Bucket> buckets = new ConcurrentHashMap<>();
    private final Map<Long, Reservation> reservations = new ConcurrentHashMap<>();

    @Autowired
    public LocalQuotaGateway(@Value("${shiyu.usage.quota.daily-token-limit:1000000}") long dailyTokenLimit,
                             @Value("${shiyu.usage.quota.concurrent-limit:8}") int defaultConcurrentLimit,
                             @Value("${shiyu.usage.quota.rpm-limit:120}") long requestsPerMinute,
                             RateLimitStore rateLimitStore) {
        this(dailyTokenLimit, defaultConcurrentLimit, Clock.systemUTC(), requestsPerMinute, rateLimitStore);
    }
    LocalQuotaGateway(long dailyTokenLimit, int defaultConcurrentLimit, Clock clock) {
        this(dailyTokenLimit, defaultConcurrentLimit, clock, Long.MAX_VALUE, new LocalRateLimitStore());
    }
    LocalQuotaGateway(long dailyTokenLimit, int defaultConcurrentLimit, Clock clock, long requestsPerMinute, RateLimitStore rateLimitStore) {
        this.dailyTokenLimit = Math.max(0, dailyTokenLimit);
        this.defaultConcurrentLimit = Math.max(1, defaultConcurrentLimit);
        this.clock = clock;
        this.requestsPerMinute = requestsPerMinute <= 0 ? Long.MAX_VALUE : requestsPerMinute;
        this.rateLimitStore = rateLimitStore == null ? new LocalRateLimitStore() : rateLimitStore;
    }

    @Override public Decision reserve(long tenantId, int promptTokens, int maxConcurrent) {
        if (tenantId <= 0 || promptTokens < 0) return new Decision(false, "QUOTA_INVALID_REQUEST", 0);
        if (!rateLimitStore.consume("tenant:" + tenantId + ":generation", 1, requestsPerMinute, Duration.ofMinutes(1))) {
            return new Decision(false, "QUOTA_RPM", 0);
        }
        Bucket bucket = buckets.computeIfAbsent(tenantId, ignored -> new Bucket());
        synchronized (bucket) {
            resetIfNeeded(bucket);
            int concurrentLimit = maxConcurrent > 0 ? Math.min(maxConcurrent, defaultConcurrentLimit) : defaultConcurrentLimit;
            if (bucket.concurrent >= concurrentLimit) return new Decision(false, "QUOTA_CONCURRENT", 0);
            if (dailyTokenLimit > 0 && bucket.tokens + bucket.reservedTokens + promptTokens > dailyTokenLimit) return new Decision(false, "QUOTA_TOKENS", 0);
            long id = reservationSequence.getAndIncrement();
            bucket.concurrent++;
            bucket.reservedTokens += promptTokens;
            reservations.put(id, new Reservation(tenantId, promptTokens));
            return new Decision(true, null, id);
        }
    }

    @Override public void settle(long tenantId, long runId, int providerPromptTokens, int providerCompletionTokens) {
        Reservation reservation = reservations.remove(runId);
        if (reservation == null || reservation.tenantId != tenantId) return;
        Bucket bucket = buckets.computeIfAbsent(tenantId, ignored -> new Bucket());
        synchronized (bucket) {
            resetIfNeeded(bucket);
            bucket.concurrent = Math.max(0, bucket.concurrent - 1);
            bucket.reservedTokens = Math.max(0, bucket.reservedTokens - reservation.promptTokens);
            bucket.tokens += Math.max(0, providerPromptTokens) + Math.max(0, providerCompletionTokens);
        }
    }

    @Override public void release(long tenantId, long runId) {
        Reservation reservation = reservations.remove(runId);
        if (reservation == null || reservation.tenantId != tenantId) return;
        Bucket bucket = buckets.computeIfAbsent(tenantId, ignored -> new Bucket());
        synchronized (bucket) {
            bucket.concurrent = Math.max(0, bucket.concurrent - 1);
            bucket.reservedTokens = Math.max(0, bucket.reservedTokens - reservation.promptTokens);
        }
    }

    private void resetIfNeeded(Bucket bucket) {
        LocalDate today = LocalDate.now(clock);
        if (!today.equals(bucket.day)) { bucket.day = today; bucket.tokens = 0; bucket.reservedTokens = 0; }
    }
    private static final class Bucket { LocalDate day = LocalDate.now(); long tokens; long reservedTokens; int concurrent; }
    private record Reservation(long tenantId, int promptTokens) { }
}
