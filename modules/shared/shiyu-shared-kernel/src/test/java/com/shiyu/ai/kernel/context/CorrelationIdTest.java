package com.shiyu.ai.kernel.context;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CorrelationIdTest {

    @Test
    void correlationIdCannotBeBlank() {
        assertThrows(NullPointerException.class, () -> new CorrelationId(null));
        assertThrows(IllegalArgumentException.class, () -> new CorrelationId("  "));
    }

    @Test
    void randomCorrelationIdIsUsable() {
        assertFalse(CorrelationId.random().value().isBlank());
    }
}
