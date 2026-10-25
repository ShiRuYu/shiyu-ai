package com.shiyu.ai.knowledge.implementation.infrastructure.task;

import com.shiyu.ai.knowledge.implementation.application.port.document.DocumentParser.ParseResult;

import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;
import com.shiyu.ai.common.thread.api.ThreadPoolManager;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.knowledge.implementation.application.port.document.DocumentParser;
import com.shiyu.ai.knowledge.implementation.application.service.rag.DocumentIngestionService;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentVersionBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeIngestionJobBO;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeEnterpriseRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 负责 Embedded Ingestion 相关数据的存储或后台处理。
 */
@Slf4j
@Component
@DependsOn("databaseInitializer")
public class EmbeddedIngestionWorker {

    /**
     * enterpriseRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeEnterpriseRepository enterpriseRepository;
    /**
     * documentRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeDocumentRepository documentRepository;
    /**
     * ingestionService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final DocumentIngestionService ingestionService;
    /**
     * objectStorage 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ObjectStorage objectStorage;
    /**
     * securityScanner 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ContentSecurityScanner securityScanner;
    /**
     * parsers 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final List<DocumentParser> parsers;
    /**
     * 调度器，表示当前对象中的对应属性。
     */
    private final ScheduledExecutorService scheduler;
    /**
     * threadPoolManager 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ThreadPoolManager threadPoolManager;
    private final Set<Long> inFlight = ConcurrentHashMap.newKeySet();
    /**
     * 执行器，表示当前对象中的对应属性。
     */
    private ExecutorService executor;
    /**
     * pollingTask 属性，保存当前对象中的业务数据或协作依赖。
     */
    private ScheduledFuture<?> pollingTask;

    /**
     * concurrency 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Value("${shiyu.knowledge.task.parse-concurrency:2}")
    private int concurrency;

    /**
     * pollDelayMs 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Value("${shiyu.knowledge.task.poll-delay-ms:1000}")
    private long pollDelayMs;

    /**
     * 执行 Embedded Ingestion 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param enterpriseRepository 用于完成本次业务处理的 enterpriseRepository 参数。
     * @param documentRepository 用于完成本次业务处理的 documentRepository 参数。
     * @param ingestionService 用于完成本次业务处理的 ingestionService 参数。
     * @param objectStorage 用于完成本次业务处理的 objectStorage 参数。
     * @param securityScanner 用于完成本次业务处理的 securityScanner 参数。
     * @param parsers 用于完成本次业务处理的 parsers 参数。
     * @param scheduler 用于完成本次业务处理的 scheduler 参数。
     * @param threadPoolManager 用于完成本次业务处理的 threadPoolManager 参数。
     */
    public EmbeddedIngestionWorker(
            KnowledgeEnterpriseRepository enterpriseRepository,
            KnowledgeDocumentRepository documentRepository,
            DocumentIngestionService ingestionService,
            ObjectStorage objectStorage,
            ContentSecurityScanner securityScanner,
            List<DocumentParser> parsers,
            ScheduledExecutorService scheduler,
            ThreadPoolManager threadPoolManager) {
        this.enterpriseRepository = enterpriseRepository;
        this.documentRepository = documentRepository;
        this.ingestionService = ingestionService;
        this.objectStorage = objectStorage;
        this.securityScanner = securityScanner;
        this.parsers = parsers;
        this.scheduler = scheduler;
        this.threadPoolManager = threadPoolManager;
        log.info("Knowledge ingestion worker constructed");
    }

    /**
     * {@code initialize} 执行当前类型定义的业务操作。
     */
    @PostConstruct
    @EventListener(ApplicationReadyEvent.class)
    public synchronized void initialize() {
        if (executor != null) return;
        log.info(
                "Knowledge ingestion worker initializing, concurrency={}, pollDelayMs={}",
                concurrency,
                pollDelayMs);
        executor = threadPoolManager.getExecutor("knowledge-ingestion");
        recoverStaleJobs();
        pollingTask =
                scheduler.scheduleWithFixedDelay(
                        this::safePoll,
                        Math.max(100, pollDelayMs),
                        Math.max(100, pollDelayMs),
                        TimeUnit.MILLISECONDS);
        log.info("Knowledge ingestion worker polling started");
    }

    private void recoverStaleJobs() {
        LocalDateTime staleBefore = LocalDateTime.now().minusMinutes(5);
        for (KnowledgeIngestionJobBO job : enterpriseRepository.findStaleJobs(staleBefore)) {
            TenantId tenantId = jobTenant(job);
            TenantScope.withTenant(
                    tenantId,
                    () -> {
                        enterpriseRepository.recoverStaleIngestionJob(
                                tenantId, job, staleBefore);
                        return null;
                    });
        }
    }

    private void safePoll() {
        try {
            poll();
        } catch (Exception exception) {
            log.error("Knowledge ingestion poll failed; the next poll will retry", exception);
        }
    }

    void poll() {
        recoverStaleJobs();
        int capacity = Math.max(0, concurrency - inFlight.size());
        if (capacity == 0) return;
        List<KnowledgeIngestionJobBO> pendingJobs = enterpriseRepository.pollPendingJobs(capacity);
        if (!pendingJobs.isEmpty()) {
            log.info("Knowledge ingestion worker claimed {} pending jobs", pendingJobs.size());
        }
        for (KnowledgeIngestionJobBO job : pendingJobs) {
            TenantId tenantId = jobTenant(job);
            if (!inFlight.add(job.getId())) continue;
            try {
                executor.submit(
                        () -> {
                            try {
                                TenantScope.withTenant(
                                        tenantId,
                                        () -> {
                                            KnowledgeIngestionJobBO claimed =
                                                    enterpriseRepository.claimIngestionJob(
                                                            tenantId, job.getId());
                                            if (claimed != null) executeClaimed(tenantId, claimed);
                                            return null;
                                        });
                            } finally {
                                inFlight.remove(job.getId());
                            }
                        });
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
                        KnowledgeIngestionJobBO job =
                                enterpriseRepository.claimIngestionJob(tenantId, jobId);
                        if (job != null) executeClaimed(tenantId, job);
                        return null;
                    });
        } finally {
            inFlight.remove(jobId);
        }
    }

    private void executeClaimed(TenantId tenantId, KnowledgeIngestionJobBO job) {
        ScheduledFuture<?> heartbeat = null;
        try {
            heartbeat =
                    scheduler.scheduleWithFixedDelay(
                            () ->
                                    TenantScope.withTenant(
                                            tenantId,
                                            () -> {
                                                enterpriseRepository.heartbeatIngestionJob(
                                                        tenantId,
                                                        job.getId(),
                                                        job.getLockVersion(),
                                                        LocalDateTime.now());
                                                return null;
                                            }),
                            30,
                            30,
                            TimeUnit.SECONDS);
            processClaimed(tenantId, job);
        } catch (Exception exception) {
            fail(tenantId, job, exception);
        } finally {
            if (heartbeat != null) heartbeat.cancel(false);
        }
    }

    private void processClaimed(TenantId tenantId, KnowledgeIngestionJobBO job) {
        KnowledgeDocumentBO document =
                documentRepository.selectById(tenantId, job.getDocumentId());
        KnowledgeDocumentVersionBO version =
                enterpriseRepository.findVersion(tenantId, job.getVersionId());
        if (document == null || version == null) {
            throw new IllegalStateException("任务关联的文档或版本不存在");
        }
        if (!job.getVersionId().equals(document.getCurrentVersionId())) {
            cancelSupersededJob(tenantId, job);
            return;
        }

        update(job, "PARSING", 30);
        DocumentParser parser =
                parserFor(document.getDocType())
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Missing document parser: "
                                                        + document.getDocType()));
        DocumentParser.ParseResult parsed;
        try {
            update(job, "READING", 10);
            byte[] bytes;
            try (ObjectStorage.ReadableObject object = objectStorage.open(version.getObjectKey())) {
                bytes = object.inputStream().readAllBytes();
                securityScanner.validate(object.originalName(), object.contentType(), bytes);
            }
            parsed = parser.parse(bytes);
        } catch (IOException storageException) {
            if (version.getContent() == null || version.getContent().isBlank()) {
                throw new IllegalStateException("文档内容不可读取", storageException);
            }
            log.warn(
                    "Source object missing, reusing stored document content, jobId={},"
                            + " objectKeyLength={}",
                    job.getId(),
                    version.getObjectKey() == null ? 0 : version.getObjectKey().length());
            parsed = new DocumentParser.ParseResult(version.getTitle(), version.getContent(), "");
        }
        if (parsed.text() == null || parsed.text().isBlank()) {
            throw new IllegalStateException("文档未解析出有效文本");
        }

        update(job, "EMBEDDING", 55);
        if (job.getActorUserId() == null || job.getActorUserId() <= 0) {
            throw new IllegalStateException(
                    "ingestion job has no actorUserId; refusing unattributed embedding");
        }
        ActorContext actor =
                new ActorContext(
                        new TenantId(job.getTenantId()), new UserId(job.getActorUserId()), false);
        version.setContent(parsed.text());
        version.setParseStatus("READY");
        document.setContent(parsed.text());
        document.setParseStatus("READY");
        if (!ingestionService.ingestIfClaimed(
                actor,
                job.getSpaceId(),
                document,
                version,
                job,
                parsed.text(),
                parsed.title(),
                List.of())) {
            throw new StaleIngestionClaimException();
        }
    }

    private void cancelSupersededJob(TenantId tenantId, KnowledgeIngestionJobBO job) {
        job.setJobStatus("CANCELLED");
        job.setStage("SUPERSEDED");
        job.setErrorMessage(null);
        job.setHeartbeatTime(LocalDateTime.now());
        job.setFinishedTime(LocalDateTime.now());
        if (!enterpriseRepository.cancelIngestionJob(tenantId, job)) {
            throw new StaleIngestionClaimException();
        }
    }

    private void update(KnowledgeIngestionJobBO job, String stage, int progress) {
        job.setStage(stage);
        job.setProgress(progress);
        job.setHeartbeatTime(LocalDateTime.now());
        if (!enterpriseRepository.updateClaimedIngestionJob(jobTenant(job), job)) {
            throw new StaleIngestionClaimException();
        }
    }

    private void fail(TenantId tenantId, KnowledgeIngestionJobBO job, Exception exception) {
        log.error("Knowledge ingestion failed, jobId={}", job.getId(), exception);
        boolean retry = job.getAttempts() < job.getMaxAttempts();
        job.setJobStatus(retry ? "PENDING" : "FAILED");
        job.setStage(retry ? "RETRY_WAIT" : "FAILED");
        job.setErrorMessage("文档处理失败，请稍后重试");
        job.setHeartbeatTime(LocalDateTime.now());
        if (!retry) job.setFinishedTime(LocalDateTime.now());
        enterpriseRepository.failClaimedIngestion(
                tenantId, job, retry ? "PENDING" : "FAILED");
    }

    private static final class StaleIngestionClaimException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private StaleIngestionClaimException() {
            super("Ingestion claim is no longer current");
        }
    }

    private TenantId jobTenant(KnowledgeIngestionJobBO job) {
        if (job == null || job.getTenantId() == null || job.getTenantId() <= 0) {
            throw new IllegalStateException("ingestion job has no valid tenantId");
        }
        return new TenantId(job.getTenantId());
    }

    private Optional<DocumentParser> parserFor(String type) {
        String normalized =
                "markdown".equalsIgnoreCase(type)
                        ? "md"
                        : "htm".equalsIgnoreCase(type) ? "html" : type;
        Optional<DocumentParser> direct =
                parsers.stream()
                        .filter(parser -> parser.getSupportedFormat().equalsIgnoreCase(normalized))
                        .findFirst();
        if (direct.isPresent()) return direct;
        // 语义文档类型（如 REFERENCE/ARTICLE/TEXTBOOK/LECTURE 等）或未知类型：
        // 回退到纯文本解析，避免后台摄取任务直接失败
        log.warn("未找到文档类型 [{}] 的解析器，回退使用纯文本解析", type);
        return parsers.stream()
                .filter(parser -> parser.getSupportedFormat().equalsIgnoreCase("txt"))
                .findFirst();
    }

    @PreDestroy
    void shutdown() {
        if (pollingTask != null) pollingTask.cancel(false);
    }
}
