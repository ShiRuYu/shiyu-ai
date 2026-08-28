package com.shiyu.ai.runtime;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultContextPolicyTest {
    private final DefaultContextPolicy policy = new DefaultContextPolicy();
    private final ContextQuery query = new ContextQuery(new com.shiyu.ai.kernel.context.TenantId(1L), new com.shiyu.ai.kernel.context.UserId(2L), "agent", "question", 5, null);

    @Test
    void acceptsOnlyItemsWithAnExplicitNonBlankAccessScope() {
        ContextItem allowed = new ContextItem("MEMORY", "m-1", "content", 0.9,
                null, List.of(), "tenant:1:user:2", Instant.now());
        assertTrue(policy.canRead(allowed, query));

        ContextItem blank = new ContextItem("MEMORY", "m-2", "content", 0.9,
                null, List.of(), " ", Instant.now());
        assertFalse(policy.canRead(blank, query));
    }

    @Test
    void rejectsMissingInputsWithoutThrowing() {
        ContextItem item = new ContextItem("MEMORY", "m-3", "content", 0.9,
                null, List.of(), "tenant:1", Instant.now());
        assertFalse(policy.canRead(null, query));
        assertFalse(policy.canRead(item, null));
        assertFalse(policy.canRead(null, null));
    }
}
