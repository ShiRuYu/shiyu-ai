package com.shiyu.ai.model.gateway;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ModelRouterTest {
    @Test
    void acceptsDefaultModelAliasAndMatchesCapabilities() {
        ModelRouter router = new ModelRouter();
        ModelRoutePolicy policy = new ModelRoutePolicy("p1", 1, "default", java.util.List.of("configured"), 1000, true, 1000);
        router.savePolicy(policy);
        assertEquals("configured", router.choose("p1", 1, Set.of("structured")).model());
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
        assertEquals("configured", router.choose("p1", 1, Set.of("chat")).model());
    }
}
