package com.shiyu.ai.kernel.context;

import java.util.Objects;
import java.util.UUID;

/** Trace identifier propagated across domain boundaries. */
public record CorrelationId(String value) {

    public CorrelationId {
        Objects.requireNonNull(value, "correlationId must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("correlationId must not be blank");
        }
    }

    public static CorrelationId random() {
        return new CorrelationId(UUID.randomUUID().toString());
    }
}
