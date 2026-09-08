package com.shiyu.ai.model.gateway;

import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelRouterDeepCoverageTest {
    @Test
    void validatesPolicyOwnershipAndCapabilityFilters() {
        ModelRouter router = new ModelRouter();
        assertThrows(IllegalArgumentException.class, () -> router.savePolicy(null));
        router.savePolicy(new ModelRoutePolicy("tenant-1", 1L, "Tenant one", List.of("configured"), 100, true, 100));
        router.savePolicy(new ModelRoutePolicy("tenant-2", 2L, "Tenant two", List.of("configured"), 100, true, 100));
        assertEquals(1, router.policies(tenant(1L)).size());
        assertThrows(IllegalArgumentException.class, () -> router.requirePolicy("tenant-1", tenant(2L)));
        assertEquals(1, router.candidates("tenant-1", tenant(1L), null).size());
        assertTrue(router.candidates("tenant-1", tenant(1L), Set.of("missing")).isEmpty());
        assertThrows(IllegalStateException.class, () -> router.choose("tenant-1", tenant(1L), Set.of("missing")));
        assertFalse(router.health("missing", "model").healthy());
    }

    @Test
    void executesFallbackForNullResultsAndHonorsFailurePolicy() {
        ModelRouter router = new ModelRouter();
        router.register(new ModelProviderCapabilities("custom", "first", Set.of("chat"), 1024));
        router.register(new ModelProviderCapabilities("custom", "second", Set.of("chat"), 1024));
        router.savePolicy(new ModelRoutePolicy("fallback", 1L, "Fallback", List.of("custom:first", "custom:second"), 100, true, 100));
        assertEquals("second", router.executeWithFallback("fallback", tenant(1L), Set.of("chat"), candidate ->
                "first".equals(candidate.model()) ? null : candidate.model()));

        router.savePolicy(new ModelRoutePolicy("failure", 1L, "Failure", List.of("custom:first", "custom:second"), 100, true, 100));
        assertEquals("second", router.executeWithFallback("failure", tenant(1L), Set.of("chat"), candidate -> {
            if ("first".equals(candidate.model())) throw new IllegalStateException("down");
            return candidate.model();
        }));
        assertEquals(1, router.health("custom", "first").consecutiveFailures());

        router.savePolicy(new ModelRoutePolicy("no-fallback", 1L, "No fallback", List.of("custom:second"), 100, false, 100));
        assertThrows(IllegalStateException.class, () -> router.executeWithFallback("no-fallback", tenant(1L), Set.of("chat"), ignored -> {
            throw new IllegalStateException("down");
        }));
        assertThrows(IllegalStateException.class, () -> router.executeWithFallback("fallback", tenant(1L), Set.of("chat"), ignored -> null));
    }

    private static TenantId tenant(long value) { return new TenantId(value); }
}
