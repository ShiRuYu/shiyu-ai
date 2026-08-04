package com.shiyu.ai.knowledge.task;

import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentVersionBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeIngestionJobBO;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.document.DocumentParser;
import com.shiyu.ai.knowledge.rag.DocumentIngestionService;
import com.shiyu.ai.common.storage.ContentSecurityScanner;
import com.shiyu.ai.common.storage.ObjectStorage;
import com.shiyu.ai.common.thread.api.ThreadPoolManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
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

    public EmbeddedIngestionWorker(KnowledgeEnterpriseRepository enterpriseRepository,
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
        log.info("Knowledge ingestion worker initializing, concurrency={}, pollDelayMs={}",
                concurrency, pollDelayMs);
        executor = threadPoolManager.getExecutor("knowledge-ingestion");
        LocalDateTime staleBefore = LocalDateTime.now().minusMinutes(5);
        for (KnowledgeIngestionJobBO job : enterpriseRepository.findStaleJobs(staleBefore)) {
            job.setJobStatus("PENDING");
            job.setStage("RECOVERED");
            job.setErrorMessage("应用重启后恢复");
            enterpriseRepository.updateJob(job);
        }
        pollingTask = scheduler.scheduleWithFixedDelay(this::safePoll, Math.max(100, pollDelayMs),
                Math.max(100, pollDelayMs), TimeUnit.MILLISECONDS);
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
                executor.submit(() -> execute(job.getId()));
            }
        }
    }

    private void execute(Long jobId) {
        KnowledgeIngestionJobBO job = enterpriseRepository.findJob(jobId);
        if (job == null) {
            inFlight.remove(jobId);
            return;
        }
        try {
            job = markRunning(job);
            if (job == null) return;
            if (isCancelled(jobId)) return;
            KnowledgeDocumentBO document = documentRepository.selectById(job.getDocumentId());
            KnowledgeDocumentVersionBO version = enterpriseRepository.findVersion(job.getVersionId());
            if (document == null || version == null) {
                throw new IllegalStateException("任务关联的文档或版本不存在");
            }

            update(job, "PARSING", 30);
            DocumentParser parser = parserFor(document.getDocType())
                    .orElseThrow(() -> new IllegalStateException("Missing document parser: " + document.getDocType()));
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
                    throw storageException;
                }
                log.warn("Source object missing, reusing stored document content, jobId={}, objectKey={}",
                        job.getId(), version.getObjectKey());
                parsed = new DocumentParser.ParseResult(version.getTitle(), version.getContent(), "");
            }
            if (parsed.text() == null || parsed.text().isBlank()) {
                throw new IllegalStateException("文档未解析出有效文本");
            }

            update(job, "EMBEDDING", 55);
            if (isCancelled(jobId)) return;
            ingestionService.ingest(job.getTenantId(), job.getSpaceId(), job.getDocumentId(), job.getVersionId(),
                    parsed.text(), List.of());

            if (isCancelled(jobId)) {
                ingestionService.delete(job.getDocumentId());
                return;
            }

            version.setContent(parsed.text());
            version.setParseStatus("READY");
            if ((version.getTitle() == null || version.getTitle().isBlank())
                    && parsed.title() != null && !parsed.title().isBlank()) {
                version.setTitle(parsed.title());
            }
            enterpriseRepository.updateVersion(version);
            document.setContent(parsed.text());
            document.setParseStatus("READY");
            documentRepository.update(document);

            job.setJobStatus("SUCCEEDED");
            job.setStage("COMPLETED");
            job.setProgress(100);
            job.setHeartbeatTime(LocalDateTime.now());
            job.setFinishedTime(LocalDateTime.now());
            enterpriseRepository.updateJob(job);
        } catch (Exception exception) {
            fail(job, exception);
        } finally {
            inFlight.remove(jobId);
        }
    }

    private KnowledgeIngestionJobBO markRunning(KnowledgeIngestionJobBO job) {
        KnowledgeIngestionJobBO current = enterpriseRepository.findJob(job.getId());
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
        enterpriseRepository.updateJob(job);
        return job;
    }

    private void update(KnowledgeIngestionJobBO job, String stage, int progress) {
        job.setStage(stage);
        job.setProgress(progress);
        job.setHeartbeatTime(LocalDateTime.now());
        enterpriseRepository.updateJob(job);
    }

    private void fail(KnowledgeIngestionJobBO job, Exception exception) {
        log.error("Knowledge ingestion failed, jobId={}", job.getId(), exception);
        if (isCancelled(job.getId())) return;
        boolean retry = job.getAttempts() < job.getMaxAttempts();
        job.setJobStatus(retry ? "PENDING" : "FAILED");
        job.setStage(retry ? "RETRY_WAIT" : "FAILED");
        job.setErrorMessage(limit(exception.getMessage(), 1900));
        job.setHeartbeatTime(LocalDateTime.now());
        if (!retry) job.setFinishedTime(LocalDateTime.now());
        enterpriseRepository.updateJob(job);
        KnowledgeDocumentBO document = documentRepository.selectById(job.getDocumentId());
        if (document != null) {
            document.setParseStatus(retry ? "PENDING" : "FAILED");
            documentRepository.update(document);
        }
    }

    private Optional<DocumentParser> parserFor(String type) {
        String normalized = "markdown".equalsIgnoreCase(type) ? "md"
                : "htm".equalsIgnoreCase(type) ? "html" : type;
        Optional<DocumentParser> direct = parsers.stream().filter(parser ->
                parser.getSupportedFormat().equalsIgnoreCase(normalized)).findFirst();
        if (direct.isPresent()) return direct;
        // 语义文档类型（如 REFERENCE/ARTICLE/TEXTBOOK/LECTURE 等）或未知类型：
        // 回退到纯文本解析，避免后台摄取任务直接失败
        log.warn("未找到文档类型 [{}] 的解析器，回退使用纯文本解析", type);
        return parsers.stream().filter(parser ->
                parser.getSupportedFormat().equalsIgnoreCase("txt")).findFirst();
    }

    private boolean isCancelled(Long jobId) {
        KnowledgeIngestionJobBO current = enterpriseRepository.findJob(jobId);
        return current != null && "CANCELLED".equals(current.getJobStatus());
    }

    private String limit(String value, int max) {
        if (value == null) return "未知错误";
        return value.length() <= max ? value : value.substring(0, max);
    }

    @PreDestroy
    void shutdown() {
        if (pollingTask != null) pollingTask.cancel(false);
    }
}
