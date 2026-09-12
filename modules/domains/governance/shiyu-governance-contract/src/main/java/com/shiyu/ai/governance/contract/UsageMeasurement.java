package com.shiyu.ai.governance.contract;

import com.shiyu.ai.kernel.event.DomainEvent;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * 记录一次模型调用的 token、费用和来源信息。
 * @param sourceType sourceType 属性，表示该记录组件承载的数据。
 * @param sourceId sourceId 属性，表示该记录组件承载的数据。
 * @param inputTokens inputTokens 属性，表示该记录组件承载的数据。
 * @param outputTokens outputTokens 属性，表示该记录组件承载的数据。
 * @param cost cost 属性，表示该记录组件承载的数据。
 * @param latencyMs latencyMs 属性，表示该记录组件承载的数据。
 * @param attributes attributes 属性，表示该记录组件承载的数据。
 */
public record UsageMeasurement(
        UsageSourceType sourceType,
        String sourceId,
        long inputTokens,
        long outputTokens,
        BigDecimal cost,
        long latencyMs,
        Map<String, String> attributes)
        implements DomainEvent {

    public UsageMeasurement(
            UsageSourceType sourceType,
            String sourceId,
            long inputTokens,
            long outputTokens,
            BigDecimal cost) {
        this(sourceType, sourceId, inputTokens, outputTokens, cost, 0L, Map.of());
    }

    public UsageMeasurement {
        Objects.requireNonNull(sourceType, "sourceType must not be null");
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(cost, "cost must not be null");
        Objects.requireNonNull(attributes, "attributes must not be null");
        if (sourceId.isBlank()) {
            throw new IllegalArgumentException("sourceId must not be blank");
        }
        if (inputTokens < 0 || outputTokens < 0) {
            throw new IllegalArgumentException("token counts must not be negative");
        }
        if (cost.signum() < 0) {
            throw new IllegalArgumentException("cost must not be negative");
        }
        if (latencyMs < 0) {
            throw new IllegalArgumentException("latencyMs must not be negative");
        }
        attributes = Map.copyOf(attributes);
    }

    @Override
    public String eventType() {
        return "governance.usage.measured";
    }
}
