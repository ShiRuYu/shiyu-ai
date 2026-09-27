package com.shiyu.ai.runtimeconsole.metrics;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;

import io.micrometer.core.instrument.MeterRegistry;

/** Samples the live process and retains no more than 30 minutes of bounded history. */
public final class ConsoleMetricsSampler implements AutoCloseable {

    private static final Duration RETENTION = Duration.ofMinutes(30);
    private static final int MAX_SAMPLES = 1800;

    private final Path appHome;
    private final DataSource dataSource;
    private final ConsoleHttpMetrics httpMetrics;
    private final Clock clock;
    private final MeterRegistry meterRegistry;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "shiyu-console-metrics");
        thread.setDaemon(true);
        return thread;
    });
    private final ArrayDeque<MetricSnapshot> samples = new ArrayDeque<>();
    private volatile long intervalMillis;
    private ScheduledFuture<?> scheduled;

    public ConsoleMetricsSampler(
            Path appHome,
            DataSource dataSource,
            ConsoleHttpMetrics httpMetrics,
            MeterRegistry meterRegistry,
            long intervalMillis) {
        this(appHome, dataSource, httpMetrics, meterRegistry, intervalMillis, Clock.systemUTC());
    }

    ConsoleMetricsSampler(
            Path appHome,
            DataSource dataSource,
            ConsoleHttpMetrics httpMetrics,
            MeterRegistry meterRegistry,
            long intervalMillis,
            Clock clock) {
        this.appHome = appHome.toAbsolutePath().normalize();
        this.dataSource = dataSource;
        this.httpMetrics = httpMetrics;
        this.meterRegistry = meterRegistry;
        this.clock = clock;
        setSampleIntervalMillis(intervalMillis);
        sampleNow();
    }

    public synchronized void setSampleIntervalMillis(long intervalMillis) {
        if (intervalMillis < 1_000 || intervalMillis > 60_000) {
            throw new IllegalArgumentException("Console sampling interval must be from 1000 to 60000 ms");
        }
        this.intervalMillis = intervalMillis;
        if (scheduled != null) {
            scheduled.cancel(false);
        }
        scheduled = executor.scheduleWithFixedDelay(
                this::sampleNow, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
    }

    public long sampleIntervalMillis() {
        return intervalMillis;
    }

    public void sampleNow() {
        Instant capturedAt = clock.instant();
        Map<String, Double> values = collectValues();
        MetricSnapshot snapshot = new MetricSnapshot(capturedAt, values);
        synchronized (samples) {
            samples.addLast(snapshot);
            Instant cutoff = capturedAt.minus(RETENTION);
            while (!samples.isEmpty() && samples.peekFirst().capturedAt().isBefore(cutoff)) {
                samples.removeFirst();
            }
            while (samples.size() > MAX_SAMPLES) {
                samples.removeFirst();
            }
        }
    }

    public List<MetricSnapshot> snapshots() {
        synchronized (samples) {
            return List.copyOf(samples);
        }
    }

    private Map<String, Double> collectValues() {
        Map<String, Double> values = new LinkedHashMap<>();
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        var heap = memory.getHeapMemoryUsage();
        values.put("jvm.heap.used", (double) heap.getUsed());
        values.put("jvm.heap.committed", (double) heap.getCommitted());
        values.put("jvm.heap.max", (double) heap.getMax());
        values.put("jvm.threads.live", (double) ManagementFactory.getThreadMXBean().getThreadCount());
        values.put("jvm.threads.peak", (double) ManagementFactory.getThreadMXBean().getPeakThreadCount());

        long gcCount = 0;
        long gcTime = 0;
        for (GarbageCollectorMXBean collector : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (collector.getCollectionCount() >= 0) gcCount += collector.getCollectionCount();
            if (collector.getCollectionTime() >= 0) gcTime += collector.getCollectionTime();
        }
        values.put("jvm.gc.count", (double) gcCount);
        values.put("jvm.gc.time-ms", (double) gcTime);

        double processCpu = -1;
        double systemCpu = -1;
        double systemMemoryUsed = -1;
        double systemMemoryTotal = -1;
        java.lang.management.OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        if (os instanceof com.sun.management.OperatingSystemMXBean extended) {
            processCpu = fractionToPercent(extended.getProcessCpuLoad());
            systemCpu = fractionToPercent(extended.getCpuLoad());
            long total = extended.getTotalMemorySize();
            long free = extended.getFreeMemorySize();
            if (total >= 0 && free >= 0) {
                systemMemoryTotal = total;
                systemMemoryUsed = total - free;
            }
        }
        values.put("process.cpu.percent", processCpu);
        values.put("system.cpu.percent", systemCpu);
        values.put("system.memory.used", systemMemoryUsed);
        values.put("system.memory.total", systemMemoryTotal);

        try {
            FileStore fileStore = Files.getFileStore(appHome);
            values.put("disk.free", (double) fileStore.getUsableSpace());
            values.put("disk.total", (double) fileStore.getTotalSpace());
        } catch (Exception ignored) {
            values.put("disk.free", -1d);
            values.put("disk.total", -1d);
        }

        PoolStats pool = readPoolStats(dataSource);
        values.put("db.active", (double) pool.active());
        values.put("db.idle", (double) pool.idle());
        values.put("db.max", (double) pool.max());
        values.put("db.pending", (double) pool.pending());

        ConsoleHttpMetrics.Window window = httpMetrics.drainWindow();
        values.put("http.requests", (double) window.requestCount());
        values.put("http.errors", (double) window.errorCount());
        values.put("http.error-rate-percent", window.requestCount() == 0 ? 0d : window.errorCount() * 100d / window.requestCount());
        values.put("http.average-latency-ms", window.requestCount() == 0 ? 0d : window.totalDurationNanos() / 1_000_000d / window.requestCount());
        values.put("micrometer.meters", (double) meterRegistry.getMeters().size());
        return values;
    }

    private static PoolStats readPoolStats(DataSource dataSource) {
        if (dataSource == null) return new PoolStats(-1, -1, -1, -1);
        try {
            Object pool = dataSource.getClass().getMethod("getHikariPoolMXBean").invoke(dataSource);
            if (pool == null) return new PoolStats(0, 0, maximumPoolSize(dataSource), 0);
            int active = number(pool, "getActiveConnections");
            int idle = number(pool, "getIdleConnections");
            int pending = number(pool, "getThreadsAwaitingConnection");
            return new PoolStats(active, idle, maximumPoolSize(dataSource), pending);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return new PoolStats(-1, -1, -1, -1);
        }
    }

    private static int maximumPoolSize(DataSource dataSource) {
        try {
            return number(dataSource, "getMaximumPoolSize");
        } catch (ReflectiveOperationException ignored) {
            return -1;
        }
    }

    private static int number(Object target, String method) throws ReflectiveOperationException {
        Object value = target.getClass().getMethod(method).invoke(target);
        return value instanceof Number number ? number.intValue() : -1;
    }

    private static double fractionToPercent(double fraction) {
        return fraction < 0 ? -1 : Math.round(fraction * 1000d) / 10d;
    }

    @Override
    public synchronized void close() {
        if (scheduled != null) scheduled.cancel(false);
        executor.shutdownNow();
    }

    private record PoolStats(int active, int idle, int max, int pending) {}
}
