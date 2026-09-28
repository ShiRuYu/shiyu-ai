package com.shiyu.ai.runtimeconsole.metrics;

import java.time.Instant;
import java.util.Map;

/** 记录带时间戳的 JVM、进程、磁盘、连接池和 HTTP 指标。 */
public record MetricSnapshot(Instant capturedAt, Map<String, Double> values) {
    public MetricSnapshot {
        values = Map.copyOf(values);
    }
}
