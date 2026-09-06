package com.shiyu.ai.governance.implementation.persistence;

import com.shiyu.ai.governance.contract.UsageSourceType;
import com.shiyu.ai.kernel.context.CorrelationId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/** Governance-owned persistence port with an atomic idempotent insert. */
@FunctionalInterface
public interface UsageLedger {

    boolean insertIfAbsent(Entry entry);

    record Entry(
            TenantId tenantId,
            UserId userId,
            CorrelationId correlationId,
            UsageSourceType sourceType,
            String sourceId,
            long inputTokens,
            long outputTokens,
            BigDecimal cost,
            Instant occurredAt
    ) {
        public Entry {
            Objects.requireNonNull(tenantId, "tenantId must not be null");
            Objects.requireNonNull(userId, "userId must not be null");
            Objects.requireNonNull(correlationId, "correlationId must not be null");
            Objects.requireNonNull(sourceType, "sourceType must not be null");
            Objects.requireNonNull(sourceId, "sourceId must not be null");
            Objects.requireNonNull(cost, "cost must not be null");
            Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        }
    }
}
