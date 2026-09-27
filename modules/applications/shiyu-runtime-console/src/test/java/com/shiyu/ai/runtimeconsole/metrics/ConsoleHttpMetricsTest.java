package com.shiyu.ai.runtimeconsole.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.junit.jupiter.api.Test;

class ConsoleHttpMetricsTest {

    @Test
    void countsBusinessRequestsButNeverCountsConsolePolling() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ConsoleHttpMetrics metrics = new ConsoleHttpMetrics(registry);

        metrics.record("GET", "/api/example", 200, 25_000_000L);
        metrics.record("GET", "/console/api/metrics", 200, 2_000_000L);
        metrics.record("POST", "/api/example", 500, 75_000_000L);

        ConsoleHttpMetrics.Window window = metrics.drainWindow();

        assertEquals(2, window.requestCount());
        assertEquals(1, window.errorCount());
        assertEquals(100_000_000L, window.totalDurationNanos());
        assertEquals(2, registry.find("http.server.requests").timers().stream().mapToLong(io.micrometer.core.instrument.Timer::count).sum());
    }
}
