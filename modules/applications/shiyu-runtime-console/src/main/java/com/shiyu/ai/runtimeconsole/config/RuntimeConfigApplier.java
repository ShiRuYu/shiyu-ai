package com.shiyu.ai.runtimeconsole.config;

import com.shiyu.ai.runtimeconsole.metrics.ConsoleMetricsSampler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerConfiguration;
import org.springframework.boot.logging.LoggingSystem;

/** Applies the narrowly scoped immediate fields with rollback on any failure. */
public final class RuntimeConfigApplier {

    private final ConsoleMetricsSampler metrics;
    private final LoggingSystem loggingSystem;
    private final Map<String, String> runtimeValues = new ConcurrentHashMap<>();

    public RuntimeConfigApplier(ConsoleMetricsSampler metrics) {
        this.metrics = metrics;
        this.loggingSystem = LoggingSystem.get(RuntimeConfigApplier.class.getClassLoader());
    }

    public void apply(Map<String, String> values) {
        LoggerConfiguration logger = loggingSystem.getLoggerConfiguration("com.shiyu");
        LogLevel previousLevel = logger == null ? null : logger.getEffectiveLevel();
        long previousInterval = metrics.sampleIntervalMillis();
        try {
            String level = values.get("logging.level.com.shiyu");
            if (level != null) {
                loggingSystem.setLogLevel("com.shiyu", LogLevel.valueOf(level.toUpperCase(java.util.Locale.ROOT)));
            }
            String sampleInterval = values.get("shiyu.console.sample-interval-ms");
            if (sampleInterval != null) {
                metrics.setSampleIntervalMillis(Long.parseLong(sampleInterval));
            }
            runtimeValues.putAll(values);
        } catch (RuntimeException failure) {
            loggingSystem.setLogLevel("com.shiyu", previousLevel);
            metrics.setSampleIntervalMillis(previousInterval);
            throw new IllegalStateException("即时运行配置应用失败，已恢复原值", failure);
        }
    }

    public String effectiveValue(String key, String fallback) {
        if (key.equals("shiyu.console.sample-interval-ms")) {
            return runtimeValues.getOrDefault(key, Long.toString(metrics.sampleIntervalMillis()));
        }
        if (key.equals("logging.level.com.shiyu")) {
            return runtimeValues.getOrDefault(key, fallback);
        }
        return runtimeValues.getOrDefault(key, fallback);
    }
}
