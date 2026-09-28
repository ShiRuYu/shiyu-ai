package com.shiyu.ai.runtimeconsole.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.atomic.LongAdder;

/** 基于 Micrometer 记录请求计数，并刻意排除控制台自身的轮询。 */
public final class ConsoleHttpMetrics {

    private final MeterRegistry registry;
    private final LongAdder requestCount = new LongAdder();
    private final LongAdder errorCount = new LongAdder();
    private final LongAdder durationNanos = new LongAdder();

    public ConsoleHttpMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void record(String method, String path, int status, long elapsedNanos) {
        if (path != null && ("/console".equals(path) || path.startsWith("/console/"))) {
            return;
        }
        long safeDuration = Math.max(0, elapsedNanos);
        requestCount.increment();
        durationNanos.add(safeDuration);
        if (status >= 400) {
            errorCount.increment();
        }
        Timer.builder("http.server.requests")
                .description("HTTP requests excluding runtime-console polling")
                .tag("method", normalize(method))
                .tag("status", Integer.toString(status))
                .register(registry)
                .record(safeDuration, java.util.concurrent.TimeUnit.NANOSECONDS);
    }

    public Window drainWindow() {
        return new Window(requestCount.sumThenReset(), errorCount.sumThenReset(), durationNanos.sumThenReset());
    }

    private static String normalize(String method) {
        if (method == null || method.isBlank()) {
            return "UNKNOWN";
        }
        return method.length() > 12 ? "OTHER" : method.toUpperCase(java.util.Locale.ROOT);
    }

    /** 表示控制台 HTTP 指标在一个采样窗口内的聚合值。 */
    public record Window(long requestCount, long errorCount, long totalDurationNanos) {}
}
