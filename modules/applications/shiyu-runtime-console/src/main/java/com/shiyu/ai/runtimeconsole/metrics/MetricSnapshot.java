package com.shiyu.ai.runtimeconsole.metrics;

import java.time.Instant;
import java.util.Map;

/** Timestamped JVM, process, disk, pool, and HTTP measurements. */
public record MetricSnapshot(Instant capturedAt, Map<String, Double> values) {
    public MetricSnapshot {
        values = Map.copyOf(values);
    }
}
