package com.shiyu.ai.runtimeconsole.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** 覆盖 JVM 指标采样、过期清理和采样间隔校验。 */
class ConsoleMetricsSamplerTest {

    @TempDir Path appHome;

    @Test
    void samplesRealJvmMetricsAndDropsValuesOlderThanThirtyMinutes() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ConsoleMetricsSampler sampler = new ConsoleMetricsSampler(
                appHome, null, new ConsoleHttpMetrics(registry), registry, 60_000, clock);
        try {
            MetricSnapshot first = sampler.snapshots().getFirst();
            assertTrue(first.values().containsKey("jvm.heap.used"));
            assertTrue(first.values().containsKey("disk.free"));

            clock.advance(Duration.ofMinutes(31));
            sampler.sampleNow();

            assertEquals(1, sampler.snapshots().size());
            assertEquals(clock.instant(), sampler.snapshots().getFirst().capturedAt());
            assertThrows(IllegalArgumentException.class, () -> sampler.setSampleIntervalMillis(500));
        } finally {
            sampler.close();
            registry.close();
        }
    }

    /** 为指标采样测试提供可控的时间源。 */
    private static final class MutableClock extends Clock {
        private final AtomicReference<Instant> instant;
        private MutableClock(Instant start) { instant = new AtomicReference<>(start); }
        private void advance(Duration duration) { instant.updateAndGet(value -> value.plus(duration)); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant.get(); }
    }
}
