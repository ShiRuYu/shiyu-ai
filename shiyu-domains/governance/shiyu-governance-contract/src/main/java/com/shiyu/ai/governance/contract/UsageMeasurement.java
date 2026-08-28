package com.shiyu.ai.governance.contract;

import com.shiyu.ai.kernel.event.DomainEvent;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/** Immutable metering event emitted once for a business operation. */
public record UsageMeasurement(
        UsageSourceType sourceType,
        String sourceId,
        long inputTokens,
        long outputTokens,
        BigDecimal cost,
        long latencyMs,
        Map<String, String> attributes
) implements DomainEvent {

    public UsageMeasurement(UsageSourceType sourceType, String sourceId,
                            long inputTokens, long outputTokens, BigDecimal cost) {
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
