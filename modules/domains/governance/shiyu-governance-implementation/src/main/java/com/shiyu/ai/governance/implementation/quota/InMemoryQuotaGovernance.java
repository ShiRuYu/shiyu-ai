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
 * 实现 In 记忆 Quota 治理 相关的业务处理、协作逻辑或基础设施能力。
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
     * 执行 In 记忆 Quota 治理 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param dailyTokenLimit 用于完成本次业务处理的 dailyTokenLimit 参数。
     * @param defaultConcurrentLimit 用于完成本次业务处理的 defaultConcurrentLimit 参数。
     * @param requestsPerMinute 用于完成本次业务处理的 requestsPerMinute 参数。
     */
    @Autowired
    public InMemoryQuotaGovernance(
            @Value("${shiyu.usage.quota.daily-token-limit:1000000}") long dailyTokenLimit,
            @Value("${shiyu.usage.quota.concurrent-limit:8}") int defaultConcurrentLimit,
            @Value("${shiyu.usage.quota.rpm-limit:120}") long requestsPerMinute) {
        this(dailyTokenLimit, defaultConcurrentLimit, requestsPerMinute, Clock.systemUTC());
    }

    /**
     * 执行 In 记忆 Quota 治理 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param dailyTokenLimit 用于完成本次业务处理的 dailyTokenLimit 参数。
     * @param defaultConcurrentLimit 用于完成本次业务处理的 defaultConcurrentLimit 参数。
     * @param requestsPerMinute 用于完成本次业务处理的 requestsPerMinute 参数。
     * @param clock 用于完成本次业务处理的 clock 参数。
     */
    public InMemoryQuotaGovernance(
            long dailyTokenLimit, int defaultConcurrentLimit, long requestsPerMinute, Clock clock) {
        this.dailyTokenLimit = Math.max(0, dailyTokenLimit);
        this.defaultConcurrentLimit = Math.max(1, defaultConcurrentLimit);
        this.requestsPerMinute = requestsPerMinute <= 0 ? Long.MAX_VALUE : requestsPerMinute;
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    /**
     * 执行 In 记忆 Quota 治理 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 In 记忆 Quota 治理 相关操作生成的结果数据。
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
     * 更新或设置 In 记忆 Quota 治理 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param reservationId 用于定位reservation的标识。
     * @param usage 用于完成本次业务处理的 usage 参数。
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
     * 执行 In 记忆 Quota 治理 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param reservationId 用于定位reservation的标识。
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
     * 实现 Bucket 相关的业务处理、协作逻辑或基础设施能力。
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
     * 封装 Reservation 相关的不可变数据及其字段约束。
     */
    private record Reservation(TenantId tenantId, int promptTokens) {}
}
