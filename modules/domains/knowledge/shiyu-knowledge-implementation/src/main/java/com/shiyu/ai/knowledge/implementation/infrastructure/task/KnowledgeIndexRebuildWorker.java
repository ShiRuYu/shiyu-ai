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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

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
        TenantScope.withTenant(
                tenantId,
                () -> {
                    KnowledgeIngestionJobBO job =
                            enterpriseRepository.claimIndexRebuildJob(tenantId, jobId);
                    if (job != null) processClaimedIndexRebuild(tenantId, job);
                    return null;
                });
    }

    private void processClaimedIndexRebuild(TenantId tenantId, KnowledgeIngestionJobBO job) {
        Long jobId = job.getId();
        ScheduledFuture<?> jobHeartbeat = null;
        ScheduledFuture<?> leaseHeartbeat = null;
        Long leaseVersion = null;
        boolean leaseClaimed = false;
        boolean buildSucceeded = false;
        boolean finalJobUpdateCompleted = false;
        AtomicBoolean leaseCurrent = new AtomicBoolean(true);
        try {
            Long claimedLeaseVersion =
                    enterpriseRepository.claimIndexRebuildLease(
                            tenantId, job.getSpaceId(), job.getId());
            if (claimedLeaseVersion == null || claimedLeaseVersion <= 0) {
                job.setJobStatus("PENDING");
                job.setStage("WAITING_FOR_SPACE_LEASE");
                job.setProgress(0);
                job.setHeartbeatTime(LocalDateTime.now());
                finalJobUpdateCompleted =
                        enterpriseRepository.updateIndexRebuildJob(tenantId, job);
                if (!finalJobUpdateCompleted) {
                    log.warn(
                            "Knowledge index rebuild job changed while waiting for lease, jobId={}",
                            job.getId());
                }
                return;
            }
            leaseVersion = claimedLeaseVersion;
            leaseClaimed = true;
            if (!enterpriseRepository.beginIndexRebuildAttempt(
                    tenantId, job.getId(), job.getLockVersion())) {
                return;
            }
            job.setAttempts((job.getAttempts() == null ? 0 : job.getAttempts()) + 1);
            job.setStage("LEASED_BUILD");
            long fencedVersion = claimedLeaseVersion;
            jobHeartbeat =
                    scheduler.scheduleWithFixedDelay(
                            () ->
                                    runHeartbeat(
                                            "job",
                                            job.getId(),
                                            () ->
                                                    TenantScope.withTenant(
                                                            tenantId,
                                                            () ->
                                                                    enterpriseRepository
                                                                            .heartbeatIndexRebuildJob(
                                                                                    tenantId,
                                                                                    job.getId(),
                                                                                    job.getLockVersion(),
                                                                                    LocalDateTime.now())),
                                            leaseCurrent),
                            30,
                            30,
                            TimeUnit.SECONDS);
            leaseHeartbeat =
                    scheduler.scheduleWithFixedDelay(
                            () ->
                                    runHeartbeat(
                                            "space lease",
                                            job.getId(),
                                            () ->
                                                    TenantScope.withTenant(
                                                            tenantId,
                                                            () ->
                                                                    enterpriseRepository
                                                                            .heartbeatIndexRebuildLease(
                                                                                    tenantId,
                                                                                    job.getSpaceId(),
                                                                                    fencedVersion,
                                                                                    LocalDateTime.now())),
                                            leaseCurrent),
                            30,
                            30,
                            TimeUnit.SECONDS);
            indexService.rebuild(
                    tenantId,
                    job.getSpaceId(),
                    fencedVersion,
                    job.getId(),
                    job.getLockVersion(),
                    leaseCurrent::get);
            if (!leaseCurrent.get()
                    || !enterpriseRepository.isIndexRebuildJobCurrent(
                            tenantId, job.getId(), job.getLockVersion())) {
                return;
            }
            job.setJobStatus("SUCCEEDED");
            job.setStage("COMPLETED");
            job.setProgress(100);
            job.setErrorMessage(null);
            job.setHeartbeatTime(LocalDateTime.now());
            job.setFinishedTime(LocalDateTime.now());
            buildSucceeded = true;
        } catch (RuntimeException exception) {
            if (!leaseCurrent.get()) return;
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
            if (jobHeartbeat != null) jobHeartbeat.cancel(false);
            if (leaseHeartbeat != null) leaseHeartbeat.cancel(false);
            if (leaseClaimed) {
                try {
                    enterpriseRepository.releaseIndexRebuildLease(
                            tenantId, job.getSpaceId(), leaseVersion, buildSucceeded);
                } catch (RuntimeException releaseFailure) {
                    log.error("Unable to release knowledge index rebuild lease, jobId={}", jobId, releaseFailure);
                }
            }
            if (!finalJobUpdateCompleted) {
                try {
                    if (!enterpriseRepository.updateIndexRebuildJob(tenantId, job)) {
                        log.warn("Knowledge index rebuild job changed before final update, jobId={}", jobId);
                    }
                } catch (RuntimeException updateFailure) {
                    log.error("Unable to update knowledge index rebuild job, jobId={}", jobId, updateFailure);
                }
            }
        }
    }

    private void runHeartbeat(
            String resource,
            Long jobId,
            BooleanSupplier heartbeat,
            AtomicBoolean leaseCurrent) {
        try {
            if (!heartbeat.getAsBoolean()) {
                leaseCurrent.set(false);
                log.warn(
                        "Knowledge index rebuild {} heartbeat was rejected, jobId={}",
                        resource,
                        jobId);
            }
        } catch (RuntimeException exception) {
            leaseCurrent.set(false);
            log.warn(
                    "Knowledge index rebuild {} heartbeat failed, jobId={}, errorType={}",
                    resource,
                    jobId,
                    exception.getClass().getSimpleName());
        }
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
