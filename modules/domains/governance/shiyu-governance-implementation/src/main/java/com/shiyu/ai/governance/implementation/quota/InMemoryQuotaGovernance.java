package com.shiyu.ai.governance.implementation.quota;

import com.shiyu.ai.common.foundation.jdbc.JdbcDialect;
import com.shiyu.ai.governance.contract.QuotaDecision;
import com.shiyu.ai.governance.contract.QuotaGovernance;
import com.shiyu.ai.governance.contract.QuotaRequest;
import com.shiyu.ai.governance.contract.QuotaUsage;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.sql.DataSource;

/**
 * 通过共享且持久化的 JDBC 状态执行租户配额策略。
 *
 * <p>为保持源码兼容而保留历史类名；运行时状态保存在治理配额表中，
 * 使用同一数据库的所有应用实例共享这些状态。
 */
@Component
public final class InMemoryQuotaGovernance implements QuotaGovernance {
    private static final String ACTIVE = "ACTIVE";
    private static final String SETTLED = "SETTLED";
    private static final String RELEASED = "RELEASED";
    private static final String EXPIRED = "EXPIRED";
    private static final String TENANT_COLUMNS =
            "DAY_START, MINUTE_START_EPOCH_SECOND, TOKENS_USED, TOKENS_RESERVED, "
                    + "REQUESTS_THIS_MINUTE, ACTIVE_RESERVATIONS";
    private static final long DEFAULT_RESERVATION_TTL_MILLIS = 300_000L;

    private final long dailyTokenLimit;
    private final int defaultConcurrentLimit;
    private final long requestsPerMinute;
    private final long reservationTtlMillis;
    private final Clock clock;
    private final JdbcTemplate jdbc;
    private final JdbcDialect dialect;
    private final TransactionTemplate transactions;

    @Autowired
    public InMemoryQuotaGovernance(
            @Value("${shiyu.usage.quota.daily-token-limit:1000000}") long dailyTokenLimit,
            @Value("${shiyu.usage.quota.concurrent-limit:8}") int defaultConcurrentLimit,
            @Value("${shiyu.usage.quota.rpm-limit:120}") long requestsPerMinute,
            @Value("${shiyu.usage.quota.reservation-ttl-ms:300000}") long reservationTtlMillis,
            @Qualifier("agentDataSource") DataSource dataSource) {
        this(
                dailyTokenLimit,
                defaultConcurrentLimit,
                requestsPerMinute,
                reservationTtlMillis,
                Clock.systemUTC(),
                dataSource);
    }

    /** 保留兼容构造方法，供直接调用方和既有集成使用。 */
    @Deprecated
    public InMemoryQuotaGovernance(
            long dailyTokenLimit, int defaultConcurrentLimit, long requestsPerMinute) {
        this(
                dailyTokenLimit,
                defaultConcurrentLimit,
                requestsPerMinute,
                DEFAULT_RESERVATION_TTL_MILLIS,
                Clock.systemUTC(),
                newEmbeddedDataSource());
    }

    /** 保留兼容构造方法，供直接调用方和既有集成使用。 */
    @Deprecated
    public InMemoryQuotaGovernance(
            long dailyTokenLimit, int defaultConcurrentLimit, long requestsPerMinute, Clock clock) {
        this(
                dailyTokenLimit,
                defaultConcurrentLimit,
                requestsPerMinute,
                DEFAULT_RESERVATION_TTL_MILLIS,
                clock,
                newEmbeddedDataSource());
    }

    public InMemoryQuotaGovernance(
            long dailyTokenLimit,
            int defaultConcurrentLimit,
            long requestsPerMinute,
            long reservationTtlMillis,
            Clock clock,
            DataSource dataSource) {
        this.dailyTokenLimit = Math.max(0, dailyTokenLimit);
        this.defaultConcurrentLimit = Math.max(1, defaultConcurrentLimit);
        this.requestsPerMinute = requestsPerMinute <= 0 ? Long.MAX_VALUE : requestsPerMinute;
        this.reservationTtlMillis = Math.max(1_000L, reservationTtlMillis);
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        Objects.requireNonNull(dataSource, "dataSource must not be null");
        this.jdbc = new JdbcTemplate(dataSource);
        this.dialect = JdbcDialect.detect(jdbc);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        this.transactions = new TransactionTemplate(transactionManager);
    }

    @Override
    public QuotaDecision reserve(ActorContext actor, QuotaRequest request) {
        return reserve(actor, request, null);
    }

    @Override
    public QuotaDecision reserve(ActorContext actor, QuotaRequest request, String referenceKey) {
        Objects.requireNonNull(actor, "actor must not be null");
        Objects.requireNonNull(request, "request must not be null");
        String normalizedReference = normalizeReference(referenceKey);
        long tenantId = actor.tenantId().value();

        return transactions.execute(
                status -> {
                    TenantState state = lockTenant(tenantId);
                    long nowMillis = state.nowMillis();
                    LocalDate today = state.dayStart();
                    if (normalizedReference != null) {
                        Reservation existing = findByReference(tenantId, normalizedReference, true);
                        if (existing != null) {
                            if (ACTIVE.equals(existing.status())
                                    && existing.expiresAtMillis() > nowMillis) {
                                return new QuotaDecision(true, null, existing.id());
                            }
                            return denied("QUOTA_RESERVATION_CLOSED");
                        }
                    }

                    if (state.requestsThisMinute() >= requestsPerMinute) {
                        return denied("QUOTA_RPM");
                    }
                    int concurrentLimit =
                            request.maxConcurrent() > 0
                                    ? Math.min(request.maxConcurrent(), defaultConcurrentLimit)
                                    : defaultConcurrentLimit;
                    if (state.activeReservations() >= concurrentLimit) {
                        return denied("QUOTA_CONCURRENT");
                    }
                    if (dailyTokenLimit > 0
                            && BigInteger.valueOf(state.tokensUsed())
                                    .add(BigInteger.valueOf(state.tokensReserved()))
                                    .add(BigInteger.valueOf(request.estimatedPromptTokens()))
                                    .compareTo(BigInteger.valueOf(dailyTokenLimit))
                                    > 0) {
                        return denied("QUOTA_TOKENS");
                    }

                    long expiresAt = Math.addExact(nowMillis, reservationTtlMillis);
                    long reservationId =
                            insertReservation(
                                    tenantId,
                                    normalizedReference,
                                    today,
                                    request.estimatedPromptTokens(),
                                    nowMillis,
                                    expiresAt);
                    jdbc.update(
                            "UPDATE GOVERNANCE_QUOTA_TENANT SET TOKENS_RESERVED=?, "
                                    + "REQUESTS_THIS_MINUTE=?, ACTIVE_RESERVATIONS=? WHERE TENANT_ID=?",
                            Math.addExact(state.tokensReserved(), request.estimatedPromptTokens()),
                            Math.addExact(state.requestsThisMinute(), 1L),
                            Math.addExact(state.activeReservations(), 1),
                            tenantId);
                    return new QuotaDecision(true, null, reservationId);
                });
    }

    @Override
    public long reservationRenewalIntervalMillis() {
        return Math.max(1L, reservationTtlMillis / 3);
    }

    @Override
    public boolean renew(ActorContext actor, String referenceKey) {
        Objects.requireNonNull(actor, "actor must not be null");
        String reference = requireReference(referenceKey);
        long tenantId = actor.tenantId().value();
        return Boolean.TRUE.equals(
                transactions.execute(
                        status -> {
                            long initialMillis = clock.millis();
                            TenantState state =
                                    lockTenant(
                                            tenantId,
                                            LocalDate.now(clock),
                                            Math.floorDiv(initialMillis, 60_000L) * 60L);
                            long nowMillis = state.nowMillis();
                            Reservation reservation = findByReference(tenantId, reference, true);
                            if (reservation == null
                                    || !ACTIVE.equals(reservation.status())
                                    || reservation.expiresAtMillis() <= nowMillis) return false;
                            long renewedExpiry = Math.addExact(nowMillis, reservationTtlMillis);
                            return jdbc.update(
                                            "UPDATE GOVERNANCE_QUOTA_RESERVATION SET EXPIRES_AT_EPOCH_MILLIS=? "
                                                    + "WHERE TENANT_ID=? AND ID=? AND STATUS=? "
                                                    + "AND EXPIRES_AT_EPOCH_MILLIS>?",
                                            renewedExpiry,
                                            tenantId,
                                            reservation.id(),
                                            ACTIVE,
                                            nowMillis)
                                    == 1;
                        }));
    }

    @Override
    public void settle(ActorContext actor, long reservationId, QuotaUsage usage) {
        Objects.requireNonNull(actor, "actor must not be null");
        Objects.requireNonNull(usage, "usage must not be null");
        transactions.executeWithoutResult(
                status -> {
                    Long tenantId = findTenantId(reservationId);
                    if (tenantId == null) return;
                    actor.requireTenant(new TenantId(tenantId));
                    long initialMillis = clock.millis();
                    LocalDate today = LocalDate.now(clock);
                    TenantState state =
                            lockTenant(
                                    tenantId,
                                    LocalDate.now(clock),
                                    Math.floorDiv(initialMillis, 60_000L) * 60L);
                    long nowMillis = state.nowMillis();
                    settleLocked(
                            tenantId,
                            findById(tenantId, reservationId, true),
                            usage,
                            state,
                            nowMillis,
                            false);
                });
    }

    @Override
    public void settle(ActorContext actor, String referenceKey, QuotaUsage usage) {
        Objects.requireNonNull(actor, "actor must not be null");
        Objects.requireNonNull(usage, "usage must not be null");
        String reference = requireReference(referenceKey);
        long tenantId = actor.tenantId().value();
        transactions.executeWithoutResult(
                status -> {
                    long initialMillis = clock.millis();
                    TenantState state =
                            lockTenant(
                                    tenantId,
                                    LocalDate.now(clock),
                                    Math.floorDiv(initialMillis, 60_000L) * 60L);
                    long nowMillis = state.nowMillis();
                    Reservation reservation = findByReference(tenantId, reference, true);
                    if (reservation == null) return;
                    QuotaUsage settledUsage =
                            usage.inputTokens() > 0
                                    ? usage
                                    : new QuotaUsage(reservation.estimatedTokens(), usage.outputTokens());
                    settleLocked(tenantId, reservation, settledUsage, state, nowMillis, true);
                });
    }

    @Override
    public void release(ActorContext actor, long reservationId) {
        Objects.requireNonNull(actor, "actor must not be null");
        transactions.executeWithoutResult(
                status -> {
                    Long tenantId = findTenantId(reservationId);
                    if (tenantId == null) return;
                    actor.requireTenant(new TenantId(tenantId));
                    long initialMillis = clock.millis();
                    TenantState state =
                            lockTenant(
                                    tenantId,
                                    LocalDate.now(clock),
                                    Math.floorDiv(initialMillis, 60_000L) * 60L);
                    long nowMillis = state.nowMillis();
                    releaseLocked(
                            tenantId,
                            findById(tenantId, reservationId, true),
                            state,
                            nowMillis);
                });
    }

    @Override
    public void release(ActorContext actor, String referenceKey) {
        Objects.requireNonNull(actor, "actor must not be null");
        String reference = requireReference(referenceKey);
        long tenantId = actor.tenantId().value();
        transactions.executeWithoutResult(
                status -> {
                    long initialMillis = clock.millis();
                    TenantState state =
                            lockTenant(
                                    tenantId,
                                    LocalDate.now(clock),
                                    Math.floorDiv(initialMillis, 60_000L) * 60L);
                    long nowMillis = state.nowMillis();
                    releaseLocked(
                            tenantId,
                            findByReference(tenantId, reference, true),
                            state,
                            nowMillis);
                });
    }

    /** 跨租户清理已放弃的过期预留，也用于应用重启后的恢复。 */
    @Scheduled(
            fixedDelayString = "${shiyu.usage.quota.expiry-scan-interval-ms:60000}",
            initialDelayString = "${shiyu.usage.quota.expiry-scan-initial-delay-ms:60000}")
    public void recoverExpiredReservations() {
        long nowMillis = clock.millis();
        List<Long> tenantIds =
                jdbc.queryForList(
                        "SELECT DISTINCT TENANT_ID FROM GOVERNANCE_QUOTA_RESERVATION "
                                + "WHERE STATUS=? AND EXPIRES_AT_EPOCH_MILLIS<=?",
                        Long.class,
                        ACTIVE,
                        nowMillis);
        for (Long tenantId : tenantIds) {
            transactions.executeWithoutResult(
                    status -> {
                        long currentMillis = clock.millis();
                        lockTenant(
                                tenantId,
                                LocalDate.now(clock),
                                Math.floorDiv(currentMillis, 60_000L) * 60L);
                    });
        }
    }

    private TenantState lockTenant(long tenantId) {
        long initialMillis = clock.millis();
        return lockTenant(
                tenantId,
                LocalDate.now(clock),
                Math.floorDiv(initialMillis, 60_000L) * 60L);
    }

    private TenantState lockTenant(long tenantId, LocalDate initialToday, long initialMinuteStart) {
        ensureTenantRow(tenantId, initialToday, initialMinuteStart);
        TenantState state =
                jdbc.queryForObject(
                        "SELECT "
                                + TENANT_COLUMNS
                                + " FROM GOVERNANCE_QUOTA_TENANT WHERE TENANT_ID=? FOR UPDATE",
                        (rs, rowNum) ->
                                new TenantState(
                                        rs.getDate("DAY_START").toLocalDate(),
                                        rs.getLong("MINUTE_START_EPOCH_SECOND"),
                                        rs.getLong("TOKENS_USED"),
                                        rs.getLong("TOKENS_RESERVED"),
                                        rs.getLong("REQUESTS_THIS_MINUTE"),
                                        rs.getInt("ACTIVE_RESERVATIONS"),
                                        0),
                        tenantId);
        long nowMillis = clock.millis();
        LocalDate today = LocalDate.now(clock);
        long minuteStart = Math.floorDiv(nowMillis, 60_000L) * 60L;
        long used = state.tokensUsed();
        long reserved = state.tokensReserved();
        long requests = state.requestsThisMinute();
        int active = state.activeReservations();
        boolean dayChanged = !today.equals(state.dayStart());
        if (dayChanged) {
            used = 0;
            reserved = 0;
        }
        if (minuteStart != state.minuteStartEpochSecond()) requests = 0;

        Integer expiredCount =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM GOVERNANCE_QUOTA_RESERVATION "
                                + "WHERE TENANT_ID=? AND STATUS=? AND EXPIRES_AT_EPOCH_MILLIS<=?",
                        Integer.class,
                        tenantId,
                        ACTIVE,
                        nowMillis);
        int expiredReservations = expiredCount == null ? 0 : expiredCount;
        if (expiredReservations > 0) {
            jdbc.update(
                    "UPDATE GOVERNANCE_QUOTA_RESERVATION SET STATUS=? WHERE TENANT_ID=? "
                            + "AND STATUS=? AND EXPIRES_AT_EPOCH_MILLIS<=?",
                    EXPIRED,
                    tenantId,
                    ACTIVE,
                    nowMillis);
        }
        if (dayChanged || expiredReservations > 0) {
            reserved = jdbc.queryForObject(
                    "SELECT COALESCE(SUM(ESTIMATED_TOKENS),0) FROM GOVERNANCE_QUOTA_RESERVATION "
                            + "WHERE TENANT_ID=? AND STATUS=? AND EXPIRES_AT_EPOCH_MILLIS>?",
                    Long.class,
                    tenantId,
                    ACTIVE,
                    nowMillis);
            active = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM GOVERNANCE_QUOTA_RESERVATION WHERE TENANT_ID=? "
                            + "AND STATUS=? AND EXPIRES_AT_EPOCH_MILLIS>?",
                    Integer.class,
                    tenantId,
                    ACTIVE,
                    nowMillis);
        }

        if (!today.equals(state.dayStart())
                || minuteStart != state.minuteStartEpochSecond()
                || expiredReservations > 0
                || dayChanged) {
            jdbc.update(
                    "UPDATE GOVERNANCE_QUOTA_TENANT SET DAY_START=?, "
                            + "MINUTE_START_EPOCH_SECOND=?, TOKENS_USED=?, TOKENS_RESERVED=?, "
                            + "REQUESTS_THIS_MINUTE=?, ACTIVE_RESERVATIONS=? WHERE TENANT_ID=?",
                    Date.valueOf(today),
                    minuteStart,
                    used,
                    reserved,
                    requests,
                    active,
                    tenantId);
        }
        return new TenantState(today, minuteStart, used, reserved, requests, active, nowMillis);
    }

    private void ensureTenantRow(long tenantId, LocalDate today, long minuteStart) {
        String sql;
        if (dialect.kind() == JdbcDialect.Kind.H2) {
            sql =
                    "MERGE INTO GOVERNANCE_QUOTA_TENANT T USING "
                            + "(VALUES (?, ?, ?)) S(TENANT_ID, DAY_START, MINUTE_START_EPOCH_SECOND) "
                            + "ON T.TENANT_ID=S.TENANT_ID WHEN NOT MATCHED THEN INSERT "
                            + "(TENANT_ID, DAY_START, MINUTE_START_EPOCH_SECOND) "
                            + "VALUES (S.TENANT_ID, S.DAY_START, S.MINUTE_START_EPOCH_SECOND)";
        } else if (dialect.kind() == JdbcDialect.Kind.POSTGRESQL) {
            sql =
                    "INSERT INTO GOVERNANCE_QUOTA_TENANT "
                            + "(TENANT_ID, DAY_START, MINUTE_START_EPOCH_SECOND) VALUES (?, ?, ?) "
                            + "ON CONFLICT (TENANT_ID) DO NOTHING";
        } else if (dialect.kind() == JdbcDialect.Kind.MYSQL) {
            sql =
                    "INSERT INTO GOVERNANCE_QUOTA_TENANT "
                            + "(TENANT_ID, DAY_START, MINUTE_START_EPOCH_SECOND) VALUES (?, ?, ?) "
                            + "ON DUPLICATE KEY UPDATE TENANT_ID=VALUES(TENANT_ID)";
        } else {
            throw new IllegalStateException("Unsupported quota database dialect: " + dialect.kind());
        }
        jdbc.update(sql, tenantId, Date.valueOf(today), minuteStart);
    }

    private long insertReservation(
            long tenantId,
            String referenceKey,
            LocalDate reservedOnDate,
            int estimatedTokens,
            long nowMillis,
            long expiresAtMillis) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(
                connection -> {
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    "INSERT INTO GOVERNANCE_QUOTA_RESERVATION "
                                            + "(TENANT_ID, REFERENCE_KEY, RESERVED_ON_DATE, ESTIMATED_TOKENS, "
                                            + "STATUS, CREATED_AT_EPOCH_MILLIS, EXPIRES_AT_EPOCH_MILLIS) "
                                            + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                                    new String[] {"id"});
                    statement.setLong(1, tenantId);
                    statement.setString(2, referenceKey);
                    statement.setDate(3, Date.valueOf(reservedOnDate));
                    statement.setInt(4, estimatedTokens);
                    statement.setString(5, ACTIVE);
                    statement.setLong(6, nowMillis);
                    statement.setLong(7, expiresAtMillis);
                    return statement;
                },
                keyHolder);
        Number key = keyHolder.getKey();
        if (key == null || key.longValue() <= 0) {
            throw new IllegalStateException("Database did not return a quota reservation ID");
        }
        return key.longValue();
    }

    private Reservation findByReference(long tenantId, String referenceKey, boolean forUpdate) {
        return findReservation("TENANT_ID=? AND REFERENCE_KEY=?", forUpdate, tenantId, referenceKey);
    }

    private Reservation findById(long tenantId, long reservationId, boolean forUpdate) {
        return findReservation("TENANT_ID=? AND ID=?", forUpdate, tenantId, reservationId);
    }

    private Reservation findReservation(String predicate, boolean forUpdate, Object... parameters) {
        try {
            return jdbc.queryForObject(
                    "SELECT ID, STATUS, ESTIMATED_TOKENS, EXPIRES_AT_EPOCH_MILLIS, RESERVED_ON_DATE "
                            + "FROM GOVERNANCE_QUOTA_RESERVATION WHERE "
                            + predicate
                            + (forUpdate ? " FOR UPDATE" : ""),
                    (rs, rowNum) ->
                            new Reservation(
                                    rs.getLong("ID"),
                                    rs.getString("STATUS"),
                                    rs.getInt("ESTIMATED_TOKENS"),
                                    rs.getLong("EXPIRES_AT_EPOCH_MILLIS"),
                                    rs.getDate("RESERVED_ON_DATE").toLocalDate()),
                    parameters);
        } catch (EmptyResultDataAccessException missing) {
            return null;
        }
    }

    private Long findTenantId(long reservationId) {
        try {
            return jdbc.queryForObject(
                    "SELECT TENANT_ID FROM GOVERNANCE_QUOTA_RESERVATION WHERE ID=?",
                    Long.class,
                    reservationId);
        } catch (EmptyResultDataAccessException missing) {
            return null;
        }
    }

    private void settleLocked(
            long tenantId,
            Reservation reservation,
            QuotaUsage usage,
            TenantState state,
            long nowMillis,
            boolean acceptExpired) {
        if (reservation == null) return;
        boolean wasActive = ACTIVE.equals(reservation.status());
        boolean wasExpired = acceptExpired && EXPIRED.equals(reservation.status());
        if (!wasActive && !wasExpired) return;
        if (wasActive && reservation.expiresAtMillis() <= nowMillis) {
            expireOne(tenantId, reservation, state);
            if (!acceptExpired) return;
            wasActive = false;
            wasExpired = true;
        }
        long actualTokens = (long) usage.inputTokens() + usage.outputTokens();
        int updated = jdbc.update(
                "UPDATE GOVERNANCE_QUOTA_RESERVATION SET STATUS=? WHERE TENANT_ID=? AND ID=? AND STATUS=?",
                SETTLED,
                tenantId,
                reservation.id(),
                wasActive ? ACTIVE : EXPIRED);
        if (updated != 1) return;
        if (wasActive) {
            jdbc.update(
                    "UPDATE GOVERNANCE_QUOTA_TENANT SET TOKENS_USED=?, TOKENS_RESERVED=?, "
                            + "ACTIVE_RESERVATIONS=? WHERE TENANT_ID=?",
                    Math.addExact(state.tokensUsed(), actualTokens),
                    reservedTokenBalance(state, reservation),
                    Math.max(0, state.activeReservations() - 1),
                    tenantId);
        } else {
            jdbc.update(
                    "UPDATE GOVERNANCE_QUOTA_TENANT SET TOKENS_USED=? WHERE TENANT_ID=?",
                    Math.addExact(state.tokensUsed(), actualTokens),
                    tenantId);
        }
    }

    private void releaseLocked(
            long tenantId, Reservation reservation, TenantState state, long nowMillis) {
        if (reservation == null || !ACTIVE.equals(reservation.status())) return;
        if (reservation.expiresAtMillis() <= nowMillis) {
            expireOne(tenantId, reservation, state);
            return;
        }
        jdbc.update(
                "UPDATE GOVERNANCE_QUOTA_RESERVATION SET STATUS=? WHERE TENANT_ID=? AND ID=? AND STATUS=?",
                RELEASED,
                tenantId,
                reservation.id(),
                ACTIVE);
        jdbc.update(
                "UPDATE GOVERNANCE_QUOTA_TENANT SET TOKENS_RESERVED=?, ACTIVE_RESERVATIONS=? "
                        + "WHERE TENANT_ID=?",
                reservedTokenBalance(state, reservation),
                Math.max(0, state.activeReservations() - 1),
                tenantId);
    }

    private long reservedTokenBalance(TenantState state, Reservation reservation) {
        return Math.max(0, state.tokensReserved() - reservation.estimatedTokens());
    }

    private void expireOne(long tenantId, Reservation reservation, TenantState state) {
        jdbc.update(
                "UPDATE GOVERNANCE_QUOTA_RESERVATION SET STATUS=? WHERE TENANT_ID=? AND ID=? AND STATUS=?",
                EXPIRED,
                tenantId,
                reservation.id(),
                ACTIVE);
        jdbc.update(
                "UPDATE GOVERNANCE_QUOTA_TENANT SET TOKENS_RESERVED=?, ACTIVE_RESERVATIONS=? "
                        + "WHERE TENANT_ID=?",
                reservedTokenBalance(state, reservation),
                Math.max(0, state.activeReservations() - 1),
                tenantId);
    }

    private static String normalizeReference(String referenceKey) {
        return referenceKey == null ? null : requireReference(referenceKey);
    }

    private static String requireReference(String referenceKey) {
        if (referenceKey == null || referenceKey.isBlank()) {
            throw new IllegalArgumentException("referenceKey must not be blank");
        }
        String normalized = referenceKey.trim();
        if (normalized.length() > 128) {
            throw new IllegalArgumentException("referenceKey must be at most 128 characters");
        }
        return normalized;
    }

    private static QuotaDecision denied(String code) {
        return new QuotaDecision(false, code, 0);
    }

    private static DataSource newEmbeddedDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(
                "jdbc:h2:mem:quota_"
                        + UUID.randomUUID().toString().replace("-", "")
                        + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        new ResourceDatabasePopulator(
                        new ClassPathResource("db/baseline/h2/schema/governance/05_governance.sql"))
                .execute(dataSource);
        return dataSource;
    }

    /** 保存持锁租户的配额余额和当前计费窗口。 */
    private record TenantState(
            LocalDate dayStart,
            long minuteStartEpochSecond,
            long tokensUsed,
            long tokensReserved,
            long requestsThisMinute,
            int activeReservations,
            long nowMillis) {}

    /** 保存配额预留的状态、估算量和到期信息。 */
    private record Reservation(
            long id,
            String status,
            int estimatedTokens,
            long expiresAtMillis,
            LocalDate reservedOnDate) {}

}
