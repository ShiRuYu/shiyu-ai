package com.shiyu.ai.common.thread.core;

import com.shiyu.ai.common.thread.api.PoolType;
import com.shiyu.ai.common.thread.config.ThreadingProperties;
import com.shiyu.ai.common.thread.context.ContextAwareRunnable;
import com.shiyu.ai.common.thread.context.ContextTaskDecorator;
import com.shiyu.ai.common.thread.context.TaskContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultThreadPoolManagerTest {

    private DefaultThreadPoolManager manager;

    @AfterEach
    void tearDown() {
        TaskContext.clear();
        if (manager != null) manager.shutdownAll();
    }

    @Test
    void createsConfiguredNamedPoolAndPropagatesTaskContext() throws Exception {
        ThreadingProperties properties = new ThreadingProperties();
        ThreadingProperties.PoolProperties worker = new ThreadingProperties.PoolProperties();
        worker.setType(PoolType.IO_INTENSIVE);
        worker.setCoreSize(1);
        worker.setMaxSize(1);
        worker.setQueueCapacity(4);
        worker.setThreadNamePrefix("test-worker");
        properties.getPools().put("knowledge-ingestion", worker);

        manager = new DefaultThreadPoolManager(properties, new ContextTaskDecorator());
        ExecutorService executor = manager.getExecutor("knowledge-ingestion");

        assertSame(executor, manager.getExecutor("knowledge-ingestion"));
        SafeExecutorService safeExecutor = (SafeExecutorService) executor;
        ThreadPoolExecutor delegate = (ThreadPoolExecutor) safeExecutor.getDelegate();
        assertEquals(1, delegate.getCorePoolSize());
        assertEquals(1, delegate.getMaximumPoolSize());
        assertEquals(4, delegate.getQueue().remainingCapacity());

        TaskContext.current().setAttribute("tenantId", 7L);
        assertEquals(7L, executor.submit(() -> TaskContext.current().getAttribute("tenantId"))
                .get(5, TimeUnit.SECONDS));
        assertTrue(delegate.getThreadFactory().newThread(() -> { }).getName().startsWith("shiyu-test-worker-io-thread-"));
    }

    @Test
    void restoresOriginalContextAfterDecoratedTaskFinishes() {
        TaskContext.current().setAttribute("tenantId", 7L);
        ContextAwareRunnable task = new ContextAwareRunnable(
                () -> TaskContext.current().setAttribute("workerOnly", true));

        task.run();

        assertEquals(Long.valueOf(7L), TaskContext.current().<Long>getAttribute("tenantId"));
        assertNull(TaskContext.current().getAttribute("workerOnly"));
    }
}
