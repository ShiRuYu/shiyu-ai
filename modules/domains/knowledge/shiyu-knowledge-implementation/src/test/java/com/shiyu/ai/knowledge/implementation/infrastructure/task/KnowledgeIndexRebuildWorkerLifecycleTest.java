package com.shiyu.ai.knowledge.implementation.infrastructure.task;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.shiyu.ai.common.thread.api.ThreadPoolManager;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeIngestionJobBO;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.implementation.infrastructure.index.service.KnowledgeIndexService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/** 验证重建工作线程的并发容量、领取失效、心跳拒绝及失败恢复边界。 */
@SuppressWarnings({"unchecked", "rawtypes"})
class KnowledgeIndexRebuildWorkerLifecycleTest {
    private static final TenantId TENANT = new TenantId(31);

    @AfterEach
    void clearContext() { TenantScope.clear(); }

    @Test
    void pollingDoesNotDuplicateInflightJobsAndRecoversCapacityAfterRejectedSubmission() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeIndexService index = mock(KnowledgeIndexService.class);
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        ExecutorService executor = mock(ExecutorService.class);
        ThreadPoolManager manager = mock(ThreadPoolManager.class);
        when(manager.getExecutor("knowledge-index-rebuild")).thenReturn(executor);
        ScheduledFuture future = mock(ScheduledFuture.class);
        List<Runnable> polls = new ArrayList<>();
        doAnswer(call -> { polls.add(call.getArgument(0)); return future; })
                .when(scheduler).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), eq(TimeUnit.MILLISECONDS));
        KnowledgeIndexRebuildWorker worker = new KnowledgeIndexRebuildWorker(repository,index,scheduler,manager);
        ReflectionTestUtils.setField(worker,"concurrency",1);
        KnowledgeIngestionJobBO job = job();
        when(repository.findStaleIndexRebuildJobs(any())).thenReturn(List.of(job));
        when(repository.pollPendingIndexRebuildJobs(1)).thenReturn(List.of(job,job));
        List<Runnable> submitted = new ArrayList<>();
        when(executor.submit(any(Runnable.class))).thenAnswer(call -> {
            if (submitted.isEmpty()) { submitted.add(null); throw new RejectedExecutionException(); }
            submitted.add(call.getArgument(0)); return null;
        });
        worker.initialize(); worker.initialize();
        assertEquals(1,polls.size());
        polls.getFirst().run();
        polls.getFirst().run();
        verify(executor,times(2)).submit(any(Runnable.class));
        worker.poll();
        verify(repository,times(2)).pollPendingIndexRebuildJobs(1);
        submitted.get(1).run();
        verify(repository).claimIndexRebuildJob(TENANT,7L);
        assertTrue(TenantScope.current().isEmpty());
        worker.shutdown();
        verify(future).cancel(false);
        verify(repository,atLeastOnce()).recoverStaleIndexRebuildJob(eq(TENANT),eq(job),any());
    }

    @Test
    void unavailableSpaceLeaseReturnsJobToWaitingWithoutConsumingAnAttempt() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeIndexService index = mock(KnowledgeIndexService.class);
        KnowledgeIngestionJobBO job = job();
        when(repository.claimIndexRebuildJob(TENANT,7L)).thenReturn(job);
        KnowledgeIndexRebuildWorker worker = worker(repository,index,mock(ScheduledExecutorService.class));
        worker.processIndexRebuild(TENANT,7L);
        assertEquals("PENDING",job.getJobStatus());
        assertEquals("WAITING_FOR_SPACE_LEASE",job.getStage());
        assertEquals(0,job.getAttempts());
        verifyNoInteractions(index);
        verify(repository,never()).beginIndexRebuildAttempt(any(),any(),any());
        when(repository.claimIndexRebuildLease(TENANT,5L,7L)).thenReturn(0L);
        worker.processIndexRebuild(TENANT,7L);
        assertEquals(0,job.getAttempts());
    }

    @Test
    void losingEitherHeartbeatPreventsPublishingSuccessAndStillReleasesLease() {
        for (boolean throwHeartbeat : List.of(false,true)) {
            KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
            KnowledgeIndexService index = mock(KnowledgeIndexService.class);
            ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
            KnowledgeIngestionJobBO job = job();
            claimed(repository,job);
            List<Runnable> heartbeats = new ArrayList<>();
            doAnswer(call -> { heartbeats.add(call.getArgument(0)); return mock(ScheduledFuture.class); })
                    .when(scheduler).scheduleWithFixedDelay(any(Runnable.class),eq(30L),eq(30L),eq(TimeUnit.SECONDS));
            if (throwHeartbeat) when(repository.heartbeatIndexRebuildLease(any(),any(),any(),any()))
                    .thenThrow(new IllegalStateException("store unavailable"));
            when(index.rebuild(eq(TENANT),eq(5L),eq(8L),eq(7L),eq(1L),any(BooleanSupplier.class)))
                    .thenAnswer(call -> {
                        heartbeats.forEach(Runnable::run);
                        assertFalse(((BooleanSupplier)call.getArgument(5)).getAsBoolean());
                        throw new IllegalStateException("lease lost during build");
                    });
            worker(repository,index,scheduler).processIndexRebuild(TENANT,7L);
            assertNotEquals("SUCCEEDED",job.getJobStatus());
            verify(repository).releaseIndexRebuildLease(TENANT,5L,8L,false);
        }
    }

    @Test
    void exhaustedRetriesBecomeFailedEvenWhenLeaseReleaseAndFinalUpdateFail() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeIndexService index = mock(KnowledgeIndexService.class);
        KnowledgeIngestionJobBO job = job(); job.setAttempts(2);
        claimed(repository,job);
        when(index.rebuild(eq(TENANT),eq(5L),eq(8L),eq(7L),eq(1L),any(BooleanSupplier.class)))
                .thenThrow(new IllegalStateException("build unavailable"));
        when(repository.releaseIndexRebuildLease(any(),any(),any(),anyBoolean()))
                .thenThrow(new IllegalStateException("release unavailable"));
        when(repository.updateIndexRebuildJob(any(),any())).thenThrow(new IllegalStateException("update unavailable"));
        assertDoesNotThrow(() -> worker(repository,index,mock(ScheduledExecutorService.class))
                .processIndexRebuild(TENANT,7L));
        assertEquals("FAILED",job.getJobStatus());
        assertNotNull(job.getFinishedTime());
        assertEquals(3,job.getAttempts());
    }

    @Test
    void attemptRejectedAfterLeaseClaimDoesNotStartIndexBuild() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeIndexService index = mock(KnowledgeIndexService.class);
        KnowledgeIngestionJobBO job = job(); claimed(repository,job);
        when(repository.beginIndexRebuildAttempt(TENANT,7L,1L)).thenReturn(false);
        worker(repository,index,mock(ScheduledExecutorService.class)).processIndexRebuild(TENANT,7L);
        verifyNoInteractions(index);
        verify(repository).releaseIndexRebuildLease(TENANT,5L,8L,false);
    }

    private static KnowledgeIndexRebuildWorker worker(KnowledgeEnterpriseRepository repository,
            KnowledgeIndexService index,ScheduledExecutorService scheduler) {
        return new KnowledgeIndexRebuildWorker(repository,index,scheduler,mock(ThreadPoolManager.class));
    }

    private static void claimed(KnowledgeEnterpriseRepository repository,KnowledgeIngestionJobBO job) {
        when(repository.claimIndexRebuildJob(TENANT,7L)).thenReturn(job);
        when(repository.claimIndexRebuildLease(TENANT,5L,7L)).thenReturn(8L);
        when(repository.beginIndexRebuildAttempt(TENANT,7L,1L)).thenReturn(true);
    }

    private static KnowledgeIngestionJobBO job() {
        KnowledgeIngestionJobBO job = new KnowledgeIngestionJobBO();
        job.setId(7L);job.setTenantId(31L);job.setSpaceId(5L);job.setLockVersion(1L);
        job.setJobStatus("RUNNING");job.setAttempts(0);job.setMaxAttempts(3);
        return job;
    }
}
