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
 * 在内存中实现配额检查、预留和结算能力。
 */
@Component
public final class InMemoryQuotaGovernance implements QuotaGovernance {
    /**
     * dailyTokenLimit 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final long dailyTokenLimit;
    /**
     * defaultConcurrentLimit 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final int defaultConcurrentLimit;
    /**
     * requestsPerMinute 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final long requestsPerMinute;
    /**
     * clock 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final Clock clock;
    private final AtomicLong reservationSequence = new AtomicLong(1);
    private final Map<TenantId, Bucket> buckets = new ConcurrentHashMap<>();
    private final Map<Long, Reservation> reservations = new ConcurrentHashMap<>();

    /**
     * {@code InMemoryQuotaGovernance} 创建并初始化当前类型实例。
     *
     * @param dailyTokenLimit 参数值，用于执行当前操作。
     * @param defaultConcurrentLimit 参数值，用于执行当前操作。
     * @param requestsPerMinute 参数值，用于执行当前操作。
     */
    @Autowired
    public InMemoryQuotaGovernance(
            @Value("${shiyu.usage.quota.daily-token-limit:1000000}") long dailyTokenLimit,
            @Value("${shiyu.usage.quota.concurrent-limit:8}") int defaultConcurrentLimit,
            @Value("${shiyu.usage.quota.rpm-limit:120}") long requestsPerMinute) {
        this(dailyTokenLimit, defaultConcurrentLimit, requestsPerMinute, Clock.systemUTC());
    }

    /**
     * {@code InMemoryQuotaGovernance} 创建并初始化当前类型实例。
     *
     * @param dailyTokenLimit 参数值，用于执行当前操作。
     * @param defaultConcurrentLimit 参数值，用于执行当前操作。
     * @param requestsPerMinute 参数值，用于执行当前操作。
     * @param clock 参数值，用于执行当前操作。
     */
    public InMemoryQuotaGovernance(
            long dailyTokenLimit, int defaultConcurrentLimit, long requestsPerMinute, Clock clock) {
        this.dailyTokenLimit = Math.max(0, dailyTokenLimit);
        this.defaultConcurrentLimit = Math.max(1, defaultConcurrentLimit);
        this.requestsPerMinute = requestsPerMinute <= 0 ? Long.MAX_VALUE : requestsPerMinute;
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    /**
     * {@code reserve} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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
            int concurrentLimit =
                    request.maxConcurrent() > 0
                            ? Math.min(request.maxConcurrent(), defaultConcurrentLimit)
                            : defaultConcurrentLimit;
            if (bucket.concurrent >= concurrentLimit) {
                return denied("QUOTA_CONCURRENT");
            }
            if (dailyTokenLimit > 0
                    && bucket.tokens + bucket.reservedTokens + request.estimatedPromptTokens()
                            > dailyTokenLimit) {
                return denied("QUOTA_TOKENS");
            }
            long reservationId = reservationSequence.getAndIncrement();
            bucket.requestsThisMinute++;
            bucket.concurrent++;
            bucket.reservedTokens += request.estimatedPromptTokens();
            reservations.put(
                    reservationId, new Reservation(tenantId, request.estimatedPromptTokens()));
            return new QuotaDecision(true, null, reservationId);
        }
    }

    /**
     * {@code settle} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param reservationId 参数值，用于执行当前操作。
     * @param usage 参数值，用于执行当前操作。
     */
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

    /**
     * {@code release} 释放或移除当前操作涉及的资源。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param reservationId 参数值，用于执行当前操作。
     */
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

    /**
     * {@code Bucket} 承载治理模块的领域状态或协作行为，负责维护本类型的职责边界。
     */
    private static final class Bucket {
        private LocalDate day = LocalDate.MIN;
        /**
         * minute 属性，保存当前对象中的业务数据或协作依赖。
         */
        private Instant minute = Instant.MIN;
        /**
         * tokens 属性，保存当前对象中的业务数据或协作依赖。
         */
        private long tokens;
        /**
         * reservedTokens 属性，保存当前对象中的业务数据或协作依赖。
         */
        private long reservedTokens;
        /**
         * requestsThisMinute 属性，保存当前对象中的业务数据或协作依赖。
         */
        private long requestsThisMinute;
        /**
         * concurrent 属性，保存当前对象中的业务数据或协作依赖。
         */
        private int concurrent;
    }

    /**
     * {@code Reservation} 封装治理模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param tenantId 租户标识，表示该记录组件承载的数据。
     * @param promptTokens promptTokens 属性，表示该记录组件承载的数据。
     */
    private record Reservation(TenantId tenantId, int promptTokens) {}
}
