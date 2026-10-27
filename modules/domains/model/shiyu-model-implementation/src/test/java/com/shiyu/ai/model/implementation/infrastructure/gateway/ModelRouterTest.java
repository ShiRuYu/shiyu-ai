package com.shiyu.ai.model.implementation.infrastructure.gateway;

import com.shiyu.ai.model.implementation.infrastructure.gateway.model.ModelRoutePolicy;

import com.shiyu.ai.model.implementation.infrastructure.gateway.service.ModelRouter;

import com.shiyu.ai.model.implementation.infrastructure.gateway.model.ModelProviderCapabilities;

import static org.junit.jupiter.api.Assertions.*;

import com.shiyu.ai.kernel.context.TenantId;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 验证 模型 Router 相关功能、边界条件、异常路径和协作行为。
 */
class ModelRouterTest {
    @Test
    void registersConfiguredDeepSeekFlashAsTheBuiltInModel() {
        ModelRouter router = new ModelRouter();
        assertTrue(router.models().stream().anyMatch(model ->
                "DEEPSEEK".equals(model.provider())
                        && "deepseek-flash".equals(model.model())));
    }

    @Test
    void acceptsDefaultModelAliasAndMatchesCapabilities() {
        ModelRouter router = new ModelRouter();
        ModelRoutePolicy policy =
                new ModelRoutePolicy(
                        "p1", 1, "default", java.util.List.of("configured"), 1000, true, 1000);
        router.savePolicy(policy);
        assertEquals("configured", router.choose("p1", tenant(1), Set.of("structured")).model());
    }

    @Test
    void rejectsUnknownModelAndEmptyRoute() {
        ModelRouter router = new ModelRouter();
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        router.savePolicy(
                                new ModelRoutePolicy(
                                        "p1",
                                        1,
                                        "bad",
                                        java.util.List.of("missing"),
                                        1000,
                                        true,
                                        1000)));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new ModelRoutePolicy(
                                "p2", 1, "empty", java.util.List.of(), 1000, true, 1000));
    }

    @Test
    void unhealthyPreferredModelFallsBackToNext() {
        ModelRouter router = new ModelRouter();
        router.register(new ModelProviderCapabilities("p", "fast", Set.of("chat"), 100));
        router.savePolicy(
                new ModelRoutePolicy(
                        "p1",
                        1,
                        "fallback",
                        java.util.List.of("p:fast", "configured"),
                        1000,
                        true,
                        1000));
        router.markFailure(tenant(1), "p", "fast", "timeout");
        router.markFailure(tenant(1), "p", "fast", "timeout");
        router.markFailure(tenant(1), "p", "fast", "timeout");
        assertEquals("configured", router.choose("p1", tenant(1), Set.of("chat")).model());
    }

    @Test
    void recordsConcurrentFailuresWithoutLostUpdates() throws Exception {
        ModelRouter router = new ModelRouter();
        int attempts = 32;
        ExecutorService executor = Executors.newFixedThreadPool(attempts);
        CountDownLatch ready = new CountDownLatch(attempts);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>(attempts);
        try {
            for (int index = 0; index < attempts; index++) {
                futures.add(
                        executor.submit(
                                () -> {
                                    ready.countDown();
                                    start.await();
                                    router.markFailure("default", "configured", "timeout");
                                    return null;
                                }));
            }
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            for (Future<?> future : futures) future.get(5, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }
        assertEquals(attempts, router.health("default", "configured").consecutiveFailures());
        assertFalse(router.health("default", "configured").healthy());
    }

    @Test
    void executesFallbackAfterProviderFailureAndRespectsNoFallbackPolicy() {
        ModelRouter router = new ModelRouter();
        router.savePolicy(
                new ModelRoutePolicy(
                        "fallback",
                        1,
                        "fallback",
                        java.util.List.of("configured", "OPENAI:gpt-4o"),
                        1000,
                        true,
                        1000));
        java.util.concurrent.atomic.AtomicInteger calls =
                new java.util.concurrent.atomic.AtomicInteger();
        String value =
                router.executeWithFallback(
                        "fallback",
                        tenant(1),
                        Set.of("chat"),
                        candidate -> {
                            if (calls.getAndIncrement() == 0)
                                throw new IllegalStateException("first down");
                            return candidate.model();
                        });
        assertEquals("gpt-4o", value);

        router.savePolicy(
                new ModelRoutePolicy(
                        "strict", 1, "strict", java.util.List.of("configured"), 1000, false, 1000));
        assertThrows(
                IllegalStateException.class,
                () ->
                        router.executeWithFallback(
                                "strict", tenant(1), Set.of("not-supported"), ignored -> "never"));
        assertThrows(
                IllegalStateException.class,
                () ->
                        router.executeWithFallback(
                                "strict",
                                tenant(1),
                                Set.of("chat"),
                                ignored -> {
                                    throw new IllegalStateException("down");
                                }));
    }

    @Test
    void isolatesRoutedHealthByTenantAndResetsItAfterCooldownProbeSucceeds() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        ModelRouter router = new ModelRouter(clock, Duration.ofSeconds(30));
        router.register(new ModelProviderCapabilities("p", "fast", Set.of("chat"), 100));
        router.savePolicy(
                new ModelRoutePolicy(
                        "tenant-1", 1, "tenant one", List.of("p:fast"), 1000, true, 1000));
        router.savePolicy(
                new ModelRoutePolicy(
                        "tenant-2", 2, "tenant two", List.of("p:fast"), 1000, true, 1000));

        for (int attempt = 0; attempt < 3; attempt++) {
            assertThrows(
                    IllegalStateException.class,
                    () ->
                            router.executeWithFallback(
                                    "tenant-1",
                                    tenant(1),
                                    Set.of("chat"),
                                    ignored -> {
                                        throw new IllegalStateException("provider down");
                                    }));
        }

        assertFalse(router.health("p", "fast", tenant(1)).healthy());
        assertTrue(router.health("p", "fast", tenant(2)).healthy());
        assertEquals(1, router.candidates("tenant-2", tenant(2), Set.of("chat")).size());
        assertTrue(router.candidates("tenant-1", tenant(1), Set.of("chat")).isEmpty());

        clock.advance(Duration.ofSeconds(31));
        assertEquals(1, router.candidates("tenant-1", tenant(1), Set.of("chat")).size());
        assertEquals(
                "recovered",
                router.executeWithFallback(
                        "tenant-1", tenant(1), Set.of("chat"), ignored -> "recovered"));
        assertEquals(0, router.health("p", "fast", tenant(1)).consecutiveFailures());
    }

    @Test
    void fallsBackWhenAProviderAttemptExceedsTheRouteTimeout() {
        ModelRouter router = new ModelRouter();
        router.register(new ModelProviderCapabilities("p", "slow", Set.of("chat"), 100));
        router.register(new ModelProviderCapabilities("p", "fast", Set.of("chat"), 100));
        router.savePolicy(
                new ModelRoutePolicy(
                        "timeout", 1, "timeout", List.of("p:slow", "p:fast"), 25, true, 1000));

        String model =
                router.executeWithFallback(
                        "timeout",
                        tenant(1),
                        Set.of("chat"),
                        candidate -> {
                            if ("slow".equals(candidate.model())) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException interrupted) {
                                    Thread.currentThread().interrupt();
                                    throw new IllegalStateException(interrupted);
                                }
                            }
                            return candidate.model();
                        });

        assertEquals("fast", model);
    }
    /** 提供测试可推进的时钟，用于确定性验证路由熔断窗口。 */

    private static final class MutableClock extends Clock {
        private final AtomicReference<Instant> current;

        private MutableClock(Instant current) {
            this.current = new AtomicReference<>(current);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return current.get();
        }

        private void advance(Duration duration) {
            current.updateAndGet(value -> value.plus(duration));
        }
    }

    @Test
    void validatesPolicyScopeAndNormalizesLimits() {
        ModelRouter router = new ModelRouter();
        assertThrows(IllegalArgumentException.class, () -> router.savePolicy(null));
        ModelRoutePolicy policy =
                new ModelRoutePolicy(
                        "p2",
                        2,
                        "limits",
                        java.util.Arrays.asList(null, " configured ", ""),
                        -1,
                        true,
                        999999);
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
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new ModelRoutePolicy(
                                null, 1, "route", java.util.List.of("model"), 1, true, 1));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new ModelRoutePolicy(
                                " ", 1, "route", java.util.List.of("model"), 1, true, 1));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new ModelRoutePolicy(
                                "id", 0, "route", java.util.List.of("model"), 1, true, 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ModelRoutePolicy("id", 1, null, java.util.List.of("model"), 1, true, 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ModelRoutePolicy("id", 1, " ", java.util.List.of("model"), 1, true, 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ModelRoutePolicy("id", 1, "route", null, 1, true, 1));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new ModelRoutePolicy(
                                "id", 1, "route", java.util.Arrays.asList(null, " "), 1, true, 1));
        ModelRoutePolicy defaults =
                new ModelRoutePolicy("id", 1, "route", java.util.List.of(" model "), 0, true, 0);
        assertEquals(java.util.List.of("model"), defaults.orderedModels());
        assertEquals(30_000, defaults.timeoutMs());
        assertEquals(16_000, defaults.maxTokens());
    }

    private static TenantId tenant(long value) {
        return new TenantId(value);
    }
}
