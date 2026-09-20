package com.shiyu.ai.governance.implementation.persistence;

import com.shiyu.ai.governance.contract.UsageSourceType;
import com.shiyu.ai.kernel.context.CorrelationId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * 定义 用量 Ledger 相关的协作契约和调用边界。
 */
@FunctionalInterface
public interface UsageLedger {

    /**
     * 创建或保存 用量 Ledger 相关业务数据，并返回处理结果。
     *
     * @param entry 用于完成本次业务处理的 entry 参数。
     * @return 返回本次条件判断是否成立。
     */
    boolean insertIfAbsent(Entry entry);

    /**
     * 封装 Entry 相关的不可变数据及其字段约束。
     */
    record Entry(
            TenantId tenantId,
            UserId userId,
            CorrelationId correlationId,
            UsageSourceType sourceType,
            String sourceId,
            long inputTokens,
            long outputTokens,
            BigDecimal cost,
            Instant occurredAt) {
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
