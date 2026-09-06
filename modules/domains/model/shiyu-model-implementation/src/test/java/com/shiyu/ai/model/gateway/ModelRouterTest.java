package com.shiyu.ai.model.gateway;

import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ModelRouterTest {
    @Test
    void acceptsDefaultModelAliasAndMatchesCapabilities() {
        ModelRouter router = new ModelRouter();
        ModelRoutePolicy policy = new ModelRoutePolicy("p1", 1, "default", java.util.List.of("configured"), 1000, true, 1000);
        router.savePolicy(policy);
        assertEquals("configured", router.choose("p1", tenant(1), Set.of("structured")).model());
    }

    @Test
    void rejectsUnknownModelAndEmptyRoute() {
        ModelRouter router = new ModelRouter();
        assertThrows(IllegalArgumentException.class, () -> router.savePolicy(new ModelRoutePolicy("p1", 1, "bad", java.util.List.of("missing"), 1000, true, 1000)));
        assertThrows(IllegalArgumentException.class, () -> new ModelRoutePolicy("p2", 1, "empty", java.util.List.of(), 1000, true, 1000));
    }

    @Test
    void unhealthyPreferredModelFallsBackToNext() {
        ModelRouter router = new ModelRouter();
        router.register(new ModelProviderCapabilities("p", "fast", Set.of("chat"), 100));
        router.savePolicy(new ModelRoutePolicy("p1", 1, "fallback", java.util.List.of("p:fast", "configured"), 1000, true, 1000));
        router.markFailure("p", "fast", "timeout");
        router.markFailure("p", "fast", "timeout");
        router.markFailure("p", "fast", "timeout");
        assertEquals("configured", router.choose("p1", tenant(1), Set.of("chat")).model());
    }

    @Test
    void executesFallbackAfterProviderFailureAndRespectsNoFallbackPolicy() {
        ModelRouter router = new ModelRouter();
        router.savePolicy(new ModelRoutePolicy("fallback", 1, "fallback",
                java.util.List.of("configured", "OPENAI:gpt-4o"), 1000, true, 1000));
        java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
        String value = router.executeWithFallback("fallback", tenant(1), Set.of("chat"), candidate -> {
            if (calls.getAndIncrement() == 0) throw new IllegalStateException("first down");
            return candidate.model();
        });
        assertEquals("gpt-4o", value);

        router.savePolicy(new ModelRoutePolicy("strict", 1, "strict",
                java.util.List.of("configured"), 1000, false, 1000));
        assertThrows(IllegalStateException.class, () -> router.executeWithFallback("strict", tenant(1),
                Set.of("not-supported"), ignored -> "never"));
        assertThrows(IllegalStateException.class, () -> router.executeWithFallback("strict", tenant(1),
                Set.of("chat"), ignored -> { throw new IllegalStateException("down"); }));
    }

    @Test
    void validatesPolicyScopeAndNormalizesLimits() {
        ModelRouter router = new ModelRouter();
        assertThrows(IllegalArgumentException.class, () -> router.savePolicy(null));
        ModelRoutePolicy policy = new ModelRoutePolicy("p2", 2, "limits",
                java.util.Arrays.asList(null, " configured ", ""), -1, true, 999999);
        assertEquals(java.util.List.of("configured"), policy.orderedModels());
        assertEquals(30_000, policy.timeoutMs());
        assertEquals(128_000, policy.maxTokens());
        router.savePolicy(policy);
        assertEquals(1, router.policies(tenant(2)).size());
        assertTrue(router.policies(null).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> router.requirePolicy("p2", tenant(1)));
        assertThrows(IllegalArgumentException.class, () -> router.requirePolicy("p2", null));
        assertFalse(router.health("unknown", "model").healthy());
    }

    @Test
    void rejectsInvalidRouteIdentityAndNormalizesBlankEntries() {
        assertThrows(IllegalArgumentException.class,
                () -> new ModelRoutePolicy(null, 1, "route", java.util.List.of("model"), 1, true, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new ModelRoutePolicy(" ", 1, "route", java.util.List.of("model"), 1, true, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new ModelRoutePolicy("id", 0, "route", java.util.List.of("model"), 1, true, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new ModelRoutePolicy("id", 1, null, java.util.List.of("model"), 1, true, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new ModelRoutePolicy("id", 1, " ", java.util.List.of("model"), 1, true, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new ModelRoutePolicy("id", 1, "route", null, 1, true, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new ModelRoutePolicy("id", 1, "route", java.util.Arrays.asList(null, " "), 1, true, 1));
        ModelRoutePolicy defaults = new ModelRoutePolicy("id", 1, "route", java.util.List.of(" model "), 0, true, 0);
        assertEquals(java.util.List.of("model"), defaults.orderedModels());
        assertEquals(30_000, defaults.timeoutMs());
        assertEquals(16_000, defaults.maxTokens());
    }

    private static TenantId tenant(long value) { return new TenantId(value); }

}
