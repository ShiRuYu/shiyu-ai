package com.shiyu.ai.knowledge.implementation.infrastructure.task;

import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
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
import com.shiyu.ai.knowledge.implementation.application.document.DocumentParser;
import com.shiyu.ai.knowledge.implementation.application.rag.DocumentIngestionService;
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

@Slf4j
@Component
@DependsOn("databaseInitializer")
public class EmbeddedIngestionWorker {

    private final KnowledgeEnterpriseRepository enterpriseRepository;
    private final KnowledgeDocumentRepository documentRepository;
    private final DocumentIngestionService ingestionService;
    private final ObjectStorage objectStorage;
    private final ContentSecurityScanner securityScanner;
    private final List<DocumentParser> parsers;
    private final ScheduledExecutorService scheduler;
    private final ThreadPoolManager threadPoolManager;
    private final Set<Long> inFlight = ConcurrentHashMap.newKeySet();
    private ExecutorService executor;
    private ScheduledFuture<?> pollingTask;

    @Value("${shiyu.knowledge.task.parse-concurrency:2}")
    private int concurrency;

    @Value("${shiyu.knowledge.task.poll-delay-ms:1000}")
    private long pollDelayMs;

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

    @PostConstruct
    @EventListener(ApplicationReadyEvent.class)
    public synchronized void initialize() {
        if (executor != null) return;
        log.info(
                "Knowledge ingestion worker initializing, concurrency={}, pollDelayMs={}",
                concurrency,
                pollDelayMs);
        executor = threadPoolManager.getExecutor("knowledge-ingestion");
        LocalDateTime staleBefore = LocalDateTime.now().minusMinutes(5);
        for (KnowledgeIngestionJobBO job : enterpriseRepository.findStaleJobs(staleBefore)) {
            job.setJobStatus("PENDING");
            job.setStage("RECOVERED");
            job.setErrorMessage("应用重启后恢复");
            TenantScope.withTenant(
                    jobTenant(job),
                    () -> {
                        enterpriseRepository.updateJob(jobTenant(job), job);
                        return null;
                    });
        }
        pollingTask =
                scheduler.scheduleWithFixedDelay(
                        this::safePoll,
                        Math.max(100, pollDelayMs),
                        Math.max(100, pollDelayMs),
                        TimeUnit.MILLISECONDS);
        log.info("Knowledge ingestion worker polling started");
    }

    private void safePoll() {
        try {
            poll();
        } catch (Exception exception) {
            log.error("Knowledge ingestion poll failed; the next poll will retry", exception);
        }
    }

    void poll() {
        int capacity = Math.max(0, concurrency - inFlight.size());
        if (capacity == 0) return;
        List<KnowledgeIngestionJobBO> pendingJobs = enterpriseRepository.pollPendingJobs(capacity);
        if (!pendingJobs.isEmpty()) {
            log.info("Knowledge ingestion worker claimed {} pending jobs", pendingJobs.size());
        }
        for (KnowledgeIngestionJobBO job : pendingJobs) {
            if (inFlight.add(job.getId())) {
                executor.submit(() -> execute(jobTenant(job), job.getId()));
            }
        }
    }

    private void execute(TenantId tenantId, Long jobId) {
        // The scheduled worker is an inbound adapter, just like the HTTP edge.
        // Bind from the persisted job rather than an unrelated request thread.
        try {
            TenantScope.withTenant(
                    tenantId,
                    () -> {
                        executeInTenant(tenantId, jobId);
                        return null;
                    });
        } finally {
            inFlight.remove(jobId);
        }
    }

    private void executeInTenant(TenantId tenantId, Long jobId) {
        KnowledgeIngestionJobBO job = enterpriseRepository.findJob(tenantId, jobId);
        if (job == null) return;
        try {
            job = markRunning(tenantId, job);
            if (job == null) return;
            if (isCancelled(tenantId, jobId)) return;
            KnowledgeDocumentBO document =
                    documentRepository.selectById(tenantId, job.getDocumentId());
            KnowledgeDocumentVersionBO version =
                    enterpriseRepository.findVersion(tenantId, job.getVersionId());
            if (document == null || version == null) {
                throw new IllegalStateException("任务关联的文档或版本不存在");
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
                try (ObjectStorage.ReadableObject object =
                        objectStorage.open(version.getObjectKey())) {
                    bytes = object.inputStream().readAllBytes();
                    securityScanner.validate(object.originalName(), object.contentType(), bytes);
                }
                parsed = parser.parse(bytes);
            } catch (IOException storageException) {
                if (version.getContent() == null || version.getContent().isBlank()) {
                    throw storageException;
                }
                log.warn(
                        "Source object missing, reusing stored document content, jobId={},"
                                + " objectKeyLength={}",
                        job.getId(),
                        version.getObjectKey() == null ? 0 : version.getObjectKey().length());
                parsed =
                        new DocumentParser.ParseResult(
                                version.getTitle(), version.getContent(), "");
            }
            if (parsed.text() == null || parsed.text().isBlank()) {
                throw new IllegalStateException("文档未解析出有效文本");
            }

            update(job, "EMBEDDING", 55);
            if (isCancelled(tenantId, jobId)) return;
            if (job.getActorUserId() == null || job.getActorUserId() <= 0) {
                throw new IllegalStateException(
                        "ingestion job has no actorUserId; refusing unattributed embedding");
            }
            ActorContext actor =
                    new ActorContext(
                            new TenantId(job.getTenantId()),
                            new UserId(job.getActorUserId()),
                            false);
            ingestionService.ingest(
                    actor,
                    job.getSpaceId(),
                    job.getDocumentId(),
                    job.getVersionId(),
                    parsed.text(),
                    List.of());

            if (isCancelled(tenantId, jobId)) {
                ingestionService.delete(
                        new com.shiyu.ai.kernel.context.TenantId(job.getTenantId()),
                        job.getDocumentId());
                return;
            }

            version.setContent(parsed.text());
            version.setParseStatus("READY");
            if ((version.getTitle() == null || version.getTitle().isBlank())
                    && parsed.title() != null
                    && !parsed.title().isBlank()) {
                version.setTitle(parsed.title());
            }
            enterpriseRepository.updateVersion(tenantId, version);
            document.setContent(parsed.text());
            document.setParseStatus("READY");
            documentRepository.update(tenantId, document);

            job.setJobStatus("SUCCEEDED");
            job.setStage("COMPLETED");
            job.setProgress(100);
            job.setHeartbeatTime(LocalDateTime.now());
            job.setFinishedTime(LocalDateTime.now());
            enterpriseRepository.updateJob(tenantId, job);
        } catch (Exception exception) {
            fail(tenantId, job, exception);
        }
    }

    private KnowledgeIngestionJobBO markRunning(TenantId tenantId, KnowledgeIngestionJobBO job) {
        KnowledgeIngestionJobBO current = enterpriseRepository.findJob(tenantId, job.getId());
        if (current == null || !"PENDING".equals(current.getJobStatus())) {
            return null;
        }
        job = current;
        job.setJobStatus("RUNNING");
        job.setStage("STARTING");
        job.setProgress(1);
        job.setAttempts((job.getAttempts() == null ? 0 : job.getAttempts()) + 1);
        job.setStartedTime(LocalDateTime.now());
        job.setHeartbeatTime(LocalDateTime.now());
        enterpriseRepository.updateJob(tenantId, job);
        return job;
    }

    private void update(KnowledgeIngestionJobBO job, String stage, int progress) {
        job.setStage(stage);
        job.setProgress(progress);
        job.setHeartbeatTime(LocalDateTime.now());
        enterpriseRepository.updateJob(jobTenant(job), job);
    }

    private void fail(TenantId tenantId, KnowledgeIngestionJobBO job, Exception exception) {
        log.error("Knowledge ingestion failed, jobId={}", job.getId(), exception);
        if (isCancelled(tenantId, job.getId())) return;
        boolean retry = job.getAttempts() < job.getMaxAttempts();
        job.setJobStatus(retry ? "PENDING" : "FAILED");
        job.setStage(retry ? "RETRY_WAIT" : "FAILED");
        job.setErrorMessage("文档处理失败，请稍后重试");
        job.setHeartbeatTime(LocalDateTime.now());
        if (!retry) job.setFinishedTime(LocalDateTime.now());
        enterpriseRepository.updateJob(tenantId, job);
        KnowledgeDocumentBO document = documentRepository.selectById(tenantId, job.getDocumentId());
        if (document != null) {
            document.setParseStatus(retry ? "PENDING" : "FAILED");
            documentRepository.update(tenantId, document);
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

    private boolean isCancelled(TenantId tenantId, Long jobId) {
        KnowledgeIngestionJobBO current = enterpriseRepository.findJob(tenantId, jobId);
        return current != null && "CANCELLED".equals(current.getJobStatus());
    }

    @PreDestroy
    void shutdown() {
        if (pollingTask != null) pollingTask.cancel(false);
    }
}
