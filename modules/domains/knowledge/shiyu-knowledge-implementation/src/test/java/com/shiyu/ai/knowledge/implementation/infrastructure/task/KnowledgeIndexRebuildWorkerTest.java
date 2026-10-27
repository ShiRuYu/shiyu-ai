package com.shiyu.ai.knowledge.implementation.infrastructure.task;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.common.thread.api.ThreadPoolManager;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeIngestionJobBO;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.implementation.infrastructure.index.service.KnowledgeIndexService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** 验证持久化知识索引重建任务的状态转换。 */
class KnowledgeIndexRebuildWorkerTest {
    private static final TenantId TENANT = new TenantId(1L);

    @AfterEach
    void clearTenantScope() {
        TenantScope.clear();
    }

    @Test
    void retriesFailedRebuildWhileAttemptsRemain() {
        TenantScope.set(TENANT);
        KnowledgeEnterpriseRepository enterprise = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeIndexService index = mock(KnowledgeIndexService.class);
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        ScheduledFuture<?> heartbeat = mock(ScheduledFuture.class);
        doReturn(heartbeat)
                .when(scheduler)
                .scheduleWithFixedDelay(
                        any(Runnable.class), eq(30L), eq(30L), eq(TimeUnit.SECONDS));
        KnowledgeIngestionJobBO job = job("RUNNING", 1, 3, 4L);
        stubClaimedJob(enterprise, job);
        doThrow(new IllegalStateException("index unavailable"))
                .when(index)
                .rebuild(
                        eq(TENANT),
                        eq(5L),
                        eq(8L),
                        eq(7L),
                        eq(4L),
                        any(java.util.function.BooleanSupplier.class));
        KnowledgeIndexRebuildWorker worker =
                new KnowledgeIndexRebuildWorker(
                        enterprise, index, scheduler, mock(ThreadPoolManager.class));

        worker.processIndexRebuild(TENANT, 7L);

        verify(enterprise)
                .updateIndexRebuildJob(
                        eq(TENANT),
                        org.mockito.ArgumentMatchers.argThat(
                                updated ->
                                        "PENDING".equals(updated.getJobStatus())
                                                && "RETRY_WAIT".equals(updated.getStage())
                                                && updated.getAttempts() == 2));
        verify(enterprise).releaseIndexRebuildLease(TENANT, 5L, 8L, false);
        verify(heartbeat, times(2)).cancel(false);
    }

    @Test
    void completesRebuildAndMarksJobSucceeded() {
        TenantScope.set(TENANT);
        KnowledgeEnterpriseRepository enterprise = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeIndexService index = mock(KnowledgeIndexService.class);
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        ScheduledFuture<?> heartbeat = mock(ScheduledFuture.class);
        doReturn(heartbeat)
                .when(scheduler)
                .scheduleWithFixedDelay(
                        any(Runnable.class), eq(30L), eq(30L), eq(TimeUnit.SECONDS));
        KnowledgeIngestionJobBO job = job("RUNNING", 1, 3, 4L);
        stubClaimedJob(enterprise, job);
        when(index.rebuild(
                        eq(TENANT),
                        eq(5L),
                        eq(8L),
                        eq(7L),
                        eq(4L),
                        any(java.util.function.BooleanSupplier.class)))
                .thenReturn(2L);
        KnowledgeIndexRebuildWorker worker =
                new KnowledgeIndexRebuildWorker(
                        enterprise, index, scheduler, mock(ThreadPoolManager.class));

        worker.processIndexRebuild(TENANT, 7L);

        verify(enterprise)
                .updateIndexRebuildJob(
                        eq(TENANT),
                        org.mockito.ArgumentMatchers.argThat(
                                updated ->
                                        "SUCCEEDED".equals(updated.getJobStatus())
                                                && "COMPLETED".equals(updated.getStage())
                                                && updated.getProgress() == 100
                                                && updated.getAttempts() == 2));
        verify(enterprise).releaseIndexRebuildLease(TENANT, 5L, 8L, true);
        verify(heartbeat, times(2)).cancel(false);
    }

    private static void stubClaimedJob(
            KnowledgeEnterpriseRepository enterprise, KnowledgeIngestionJobBO job) {
        when(enterprise.claimIndexRebuildJob(TENANT, 7L)).thenReturn(job);
        when(enterprise.claimIndexRebuildLease(TENANT, 5L, 7L)).thenReturn(8L);
        when(enterprise.beginIndexRebuildAttempt(TENANT, 7L, 4L)).thenReturn(true);
        when(enterprise.isIndexRebuildJobCurrent(TENANT, 7L, 4L)).thenReturn(true);
        when(enterprise.updateIndexRebuildJob(eq(TENANT), any())).thenReturn(true);
        when(enterprise.releaseIndexRebuildLease(TENANT, 5L, 8L, true)).thenReturn(true);
        when(enterprise.releaseIndexRebuildLease(TENANT, 5L, 8L, false)).thenReturn(true);
    }

    private static KnowledgeIngestionJobBO job(
            String status, int attempts, int maxAttempts, Long lockVersion) {
        KnowledgeIngestionJobBO job = new KnowledgeIngestionJobBO();
        job.setId(7L);
        job.setTenantId(1L);
        job.setJobType("INDEX_REBUILD");
        job.setSpaceId(5L);
        job.setJobStatus(status);
        job.setAttempts(attempts);
        job.setMaxAttempts(maxAttempts);
        job.setLockVersion(lockVersion);
        return job;
    }
}
