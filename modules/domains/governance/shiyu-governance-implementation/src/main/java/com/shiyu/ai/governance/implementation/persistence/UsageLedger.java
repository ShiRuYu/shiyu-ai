package com.shiyu.ai.governance.implementation.persistence;

import com.shiyu.ai.governance.contract.UsageSourceType;
import com.shiyu.ai.kernel.context.CorrelationId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * UsageLedger 接口，定义治理模块的能力边界。
 */
@FunctionalInterface
public interface UsageLedger {

    /**
     * 创建并保存业务对象。
     *
     * @param entry 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean insertIfAbsent(Entry entry);

    /**
     * {@code Entry} 封装治理模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param tenantId 租户标识，表示该记录组件承载的数据。
     * @param userId 用户标识，表示该记录组件承载的数据。
     * @param correlationId correlationId 属性，表示该记录组件承载的数据。
     * @param sourceType sourceType 属性，表示该记录组件承载的数据。
     * @param sourceId sourceId 属性，表示该记录组件承载的数据。
     * @param inputTokens inputTokens 属性，表示该记录组件承载的数据。
     * @param outputTokens outputTokens 属性，表示该记录组件承载的数据。
     * @param cost cost 属性，表示该记录组件承载的数据。
     * @param occurredAt occurredAt 属性，表示该记录组件承载的数据。
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
