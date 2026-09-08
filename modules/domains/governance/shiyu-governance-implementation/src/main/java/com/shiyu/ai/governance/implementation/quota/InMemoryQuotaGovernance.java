package com.shiyu.ai.governance.implementation.quota;

import com.shiyu.ai.governance.contract.QuotaDecision;
import com.shiyu.ai.governance.contract.QuotaGovernance;
import com.shiyu.ai.governance.contract.QuotaRequest;
import com.shiyu.ai.governance.contract.QuotaUsage;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Single-node Governance quota implementation.
 *
 * The contract is deliberately actor-first so callers cannot reserve capacity
 * without proving both tenant and user identity. A distributed lease adapter
 * can replace this implementation without changing the bounded-context API.
 */
@Component
public final class InMemoryQuotaGovernance implements QuotaGovernance {
    private final long dailyTokenLimit;
    private final int defaultConcurrentLimit;
    private final long requestsPerMinute;
    private final Clock clock;
    private final AtomicLong reservationSequence = new AtomicLong(1);
    private final Map<TenantId, Bucket> buckets = new ConcurrentHashMap<>();
    private final Map<Long, Reservation> reservations = new ConcurrentHashMap<>();

    @Autowired
    public InMemoryQuotaGovernance(
            @Value("${shiyu.usage.quota.daily-token-limit:1000000}") long dailyTokenLimit,
            @Value("${shiyu.usage.quota.concurrent-limit:8}") int defaultConcurrentLimit,
            @Value("${shiyu.usage.quota.rpm-limit:120}") long requestsPerMinute) {
        this(dailyTokenLimit, defaultConcurrentLimit, requestsPerMinute, Clock.systemUTC());
    }

    public InMemoryQuotaGovernance(long dailyTokenLimit, int defaultConcurrentLimit,
                                   long requestsPerMinute, Clock clock) {
        this.dailyTokenLimit = Math.max(0, dailyTokenLimit);
        this.defaultConcurrentLimit = Math.max(1, defaultConcurrentLimit);
        this.requestsPerMinute = requestsPerMinute <= 0 ? Long.MAX_VALUE : requestsPerMinute;
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public QuotaDecision reserve(ActorContext actor, QuotaRequest request) {
        Objects.requireNonNull(actor, "actor must not be null");
        Objects.requireNonNull(request, "request must not be null");
        TenantId tenantId = actor.tenantId();
        Bucket bucket = buckets.computeIfAbsent(tenantId, ignored -> new Bucket());
        synchronized (bucket) {
            resetIfNeeded(bucket);
            if (bucket.requestsThisMinute >= requestsPerMinute) {
                return denied("QUOTA_RPM");
            }
            int concurrentLimit = request.maxConcurrent() > 0
                    ? Math.min(request.maxConcurrent(), defaultConcurrentLimit)
                    : defaultConcurrentLimit;
            if (bucket.concurrent >= concurrentLimit) {
                return denied("QUOTA_CONCURRENT");
            }
            if (dailyTokenLimit > 0
                    && bucket.tokens + bucket.reservedTokens + request.estimatedPromptTokens() > dailyTokenLimit) {
                return denied("QUOTA_TOKENS");
            }
            long reservationId = reservationSequence.getAndIncrement();
            bucket.requestsThisMinute++;
            bucket.concurrent++;
            bucket.reservedTokens += request.estimatedPromptTokens();
            reservations.put(reservationId,
                    new Reservation(tenantId, request.estimatedPromptTokens()));
            return new QuotaDecision(true, null, reservationId);
        }
    }

    @Override
    public void settle(ActorContext actor, long reservationId, QuotaUsage usage) {
        Objects.requireNonNull(actor, "actor must not be null");
        Objects.requireNonNull(usage, "usage must not be null");
        Reservation reservation = reservations.get(reservationId);
        if (reservation == null) return;
        actor.requireTenant(reservation.tenantId);
        if (!reservations.remove(reservationId, reservation)) return;
        Bucket bucket = buckets.computeIfAbsent(reservation.tenantId, ignored -> new Bucket());
        synchronized (bucket) {
            resetIfNeeded(bucket);
            bucket.concurrent = Math.max(0, bucket.concurrent - 1);
            bucket.reservedTokens = Math.max(0, bucket.reservedTokens - reservation.promptTokens);
            bucket.tokens += (long) usage.inputTokens() + usage.outputTokens();
        }
    }

    @Override
    public void release(ActorContext actor, long reservationId) {
        Objects.requireNonNull(actor, "actor must not be null");
        Reservation reservation = reservations.get(reservationId);
        if (reservation == null) return;
        actor.requireTenant(reservation.tenantId);
        if (!reservations.remove(reservationId, reservation)) return;
        Bucket bucket = buckets.computeIfAbsent(reservation.tenantId, ignored -> new Bucket());
        synchronized (bucket) {
            resetIfNeeded(bucket);
            bucket.concurrent = Math.max(0, bucket.concurrent - 1);
            bucket.reservedTokens = Math.max(0, bucket.reservedTokens - reservation.promptTokens);
        }
    }

    private void resetIfNeeded(Bucket bucket) {
        LocalDate today = LocalDate.now(clock);
        Instant now = Instant.now(clock);
        Instant minute = now.minusSeconds(now.getEpochSecond() % 60);
        if (!today.equals(bucket.day)) {
            bucket.day = today;
            bucket.tokens = 0;
            bucket.reservedTokens = 0;
        }
        if (!minute.equals(bucket.minute)) {
            bucket.minute = minute;
            bucket.requestsThisMinute = 0;
        }
    }

    private static QuotaDecision denied(String code) {
        return new QuotaDecision(false, code, 0);
    }

    private static final class Bucket {
        private LocalDate day = LocalDate.MIN;
        private Instant minute = Instant.MIN;
        private long tokens;
        private long reservedTokens;
        private long requestsThisMinute;
        private int concurrent;
    }

    private record Reservation(TenantId tenantId, int promptTokens) { }
}
