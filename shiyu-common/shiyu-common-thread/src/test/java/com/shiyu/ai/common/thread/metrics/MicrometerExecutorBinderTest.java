package com.shiyu.ai.common.thread.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MicrometerExecutorBinder 单元测试
 */
@Tag("dev")
class MicrometerExecutorBinderTest {

    private MeterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
    }

    @Test
    void testBindThreadPoolExecutor() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        MicrometerExecutorBinder binder = new MicrometerExecutorBinder(executor, "test-pool");

        binder.bindTo(registry);

        assertNotNull(registry.find("executor.test-pool.core.pool.size").gauge());
        assertNotNull(registry.find("executor.test-pool.max.pool.size").gauge());
        assertNotNull(registry.find("executor.test-pool.active.count").gauge());
        assertNotNull(registry.find("executor.test-pool.pool.size").gauge());
        assertNotNull(registry.find("executor.test-pool.queue.size").gauge());
        assertNotNull(registry.find("executor.test-pool.queue.remaining.capacity").gauge());
        assertNotNull(registry.find("executor.test-pool.completed.task.count").gauge());
        assertNotNull(registry.find("executor.test-pool.task.count").gauge());
    }

    @Test
    void testBindExecutorWithCorrectValues() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        MicrometerExecutorBinder binder = new MicrometerExecutorBinder(executor, "test-pool");

        binder.bindTo(registry);

        Gauge corePoolGauge = registry.find("executor.test-pool.core.pool.size").gauge();
        assertNotNull(corePoolGauge);
        assertEquals(4.0, corePoolGauge.value(), 0.001);
    }

    @Test
    void testCreateTaskTimer() {
        Timer timer = MicrometerExecutorBinder.createTaskTimer(registry, "test-pool");

        assertNotNull(timer);
        assertEquals("executor.task.execution", timer.getId().getName());
        assertEquals("test-pool", timer.getId().getTag("name"));
    }

    @Test
    void testCreateSubmissionTimer() {
        Timer timer = MicrometerExecutorBinder.createSubmissionTimer(registry, "test-pool");

        assertNotNull(timer);
        assertEquals("executor.task.submission", timer.getId().getName());
        assertEquals("test-pool", timer.getId().getTag("name"));
    }
}
