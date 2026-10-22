package com.shiyu.ai.knowledge.implementation.infrastructure.task;

import com.shiyu.ai.common.thread.api.ThreadPoolManager;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeIngestionJobBO;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.implementation.infrastructure.index.service.KnowledgeIndexService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** Processes durable knowledge-space index rebuild jobs. */
@Slf4j
@Component
@DependsOn("databaseInitializer")
public class KnowledgeIndexRebuildWorker {
    private final KnowledgeEnterpriseRepository enterpriseRepository;
    private final KnowledgeIndexService indexService;
    private final ScheduledExecutorService scheduler;
    private final ThreadPoolManager threadPoolManager;
    private final Set<Long> inFlight = ConcurrentHashMap.newKeySet();
    private ExecutorService executor;
    private ScheduledFuture<?> pollingTask;

    @Value("${shiyu.knowledge.index.rebuild-concurrency:1}")
    private int concurrency;

    @Value("${shiyu.knowledge.index.rebuild-poll-delay-ms:3000}")
    private long pollDelayMs;

    @Value("${shiyu.knowledge.index.rebuild-lease-timeout-minutes:5}")
    private long leaseTimeoutMinutes;

    public KnowledgeIndexRebuildWorker(
            KnowledgeEnterpriseRepository enterpriseRepository,
            KnowledgeIndexService indexService,
            ScheduledExecutorService scheduler,
            ThreadPoolManager threadPoolManager) {
        this.enterpriseRepository = enterpriseRepository;
        this.indexService = indexService;
        this.scheduler = scheduler;
        this.threadPoolManager = threadPoolManager;
    }

    @PostConstruct
    @EventListener(ApplicationReadyEvent.class)
    public synchronized void initialize() {
        if (executor != null) return;
        executor = threadPoolManager.getExecutor("knowledge-index-rebuild");
        pollingTask =
                scheduler.scheduleWithFixedDelay(
                        this::safePoll,
                        Math.max(100, pollDelayMs),
                        Math.max(100, pollDelayMs),
                        TimeUnit.MILLISECONDS);
    }

    private void safePoll() {
        try {
            poll();
        } catch (Exception exception) {
            log.error("Knowledge index rebuild poll failed; the next poll will retry", exception);
        }
    }

    void poll() {
        LocalDateTime heartbeatBefore = LocalDateTime.now().minusMinutes(leaseTimeoutMinutes);
        for (KnowledgeIngestionJobBO stale :
                enterpriseRepository.findStaleIndexRebuildJobs(heartbeatBefore)) {
            TenantId tenantId = jobTenant(stale);
            TenantScope.withTenant(
                    tenantId,
                    () -> {
                        enterpriseRepository.recoverStaleIndexRebuildJob(
                                tenantId, stale, heartbeatBefore);
                        return null;
                    });
        }

        int capacity = Math.max(0, concurrency - inFlight.size());
        if (capacity == 0) return;
        for (KnowledgeIngestionJobBO job :
                enterpriseRepository.pollPendingIndexRebuildJobs(capacity)) {
            if (!inFlight.add(job.getId())) continue;
            try {
                executor.submit(() -> execute(jobTenant(job), job.getId()));
            } catch (RuntimeException exception) {
                inFlight.remove(job.getId());
                throw exception;
            }
        }
    }

    private void execute(TenantId tenantId, Long jobId) {
        try {
            TenantScope.withTenant(
                    tenantId,
                    () -> {
                        processIndexRebuild(tenantId, jobId);
                        return null;
                    });
        } finally {
            inFlight.remove(jobId);
        }
    }

    void processIndexRebuild(TenantId tenantId, Long jobId) {
        KnowledgeIngestionJobBO job =
                enterpriseRepository.claimIndexRebuildJob(tenantId, jobId);
        if (job == null) return;
        ScheduledFuture<?> heartbeatTask = null;
        try {
            heartbeatTask =
                    scheduler.scheduleWithFixedDelay(
                            () ->
                                    enterpriseRepository.heartbeatIndexRebuildJob(
                                            tenantId,
                                            jobId,
                                            job.getLockVersion(),
                                            LocalDateTime.now()),
                            30,
                            30,
                            TimeUnit.SECONDS);
            indexService.rebuild(tenantId, job.getSpaceId());
            job.setJobStatus("SUCCEEDED");
            job.setStage("COMPLETED");
            job.setProgress(100);
            job.setErrorMessage(null);
            job.setHeartbeatTime(LocalDateTime.now());
            job.setFinishedTime(LocalDateTime.now());
        } catch (RuntimeException exception) {
            boolean retry = job.getAttempts() < job.getMaxAttempts();
            job.setJobStatus(retry ? "PENDING" : "FAILED");
            job.setStage(retry ? "RETRY_WAIT" : "FAILED");
            job.setProgress(0);
            job.setErrorMessage("知识空间索引重建失败");
            job.setHeartbeatTime(LocalDateTime.now());
            if (!retry) job.setFinishedTime(LocalDateTime.now());
            log.warn(
                    "Knowledge index rebuild failed, jobId={}, retry={}, errorType={}",
                    jobId,
                    retry,
                    exception.getClass().getSimpleName());
        } finally {
            if (heartbeatTask != null) heartbeatTask.cancel(false);
        }
        enterpriseRepository.updateIndexRebuildJob(tenantId, job);
    }

    private TenantId jobTenant(KnowledgeIngestionJobBO job) {
        if (job == null || job.getTenantId() == null || job.getTenantId() <= 0) {
            throw new IllegalStateException("index rebuild job has no valid tenantId");
        }
        return new TenantId(job.getTenantId());
    }

    @PreDestroy
    void shutdown() {
        if (pollingTask != null) pollingTask.cancel(false);
    }
}
