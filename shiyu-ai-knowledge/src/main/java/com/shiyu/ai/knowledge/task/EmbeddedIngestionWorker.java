package com.shiyu.ai.knowledge.task;

import com.shiyu.ai.dal.knowledge.bo.KnowledgeDocumentBO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDocumentVersionDO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeIngestionJobDO;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.document.DocumentParser;
import com.shiyu.ai.knowledge.rag.DocumentIngestionService;
import com.shiyu.ai.knowledge.storage.ContentSecurityScanner;
import com.shiyu.ai.knowledge.storage.ObjectStorage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@DependsOn("embeddedDatabaseMigrations")
public class EmbeddedIngestionWorker {

    private final KnowledgeEnterpriseRepository enterpriseRepository;
    private final KnowledgeDocumentRepository documentRepository;
    private final DocumentIngestionService ingestionService;
    private final ObjectStorage objectStorage;
    private final ContentSecurityScanner securityScanner;
    private final List<DocumentParser> parsers;
    private final Set<Long> inFlight = ConcurrentHashMap.newKeySet();
    private ExecutorService executor;

    @Value("${shiyu.knowledge.task.parse-concurrency:2}")
    private int concurrency;

    public EmbeddedIngestionWorker(KnowledgeEnterpriseRepository enterpriseRepository,
                                   KnowledgeDocumentRepository documentRepository,
                                   DocumentIngestionService ingestionService,
                                   ObjectStorage objectStorage,
                                   ContentSecurityScanner securityScanner,
                                   List<DocumentParser> parsers) {
        this.enterpriseRepository = enterpriseRepository;
        this.documentRepository = documentRepository;
        this.ingestionService = ingestionService;
        this.objectStorage = objectStorage;
        this.securityScanner = securityScanner;
        this.parsers = parsers;
    }

    @PostConstruct
    void initialize() {
        executor = Executors.newFixedThreadPool(Math.max(1, concurrency),
                Thread.ofPlatform().name("knowledge-ingest-", 0).factory());
        LocalDateTime staleBefore = LocalDateTime.now().minusMinutes(5);
        for (KnowledgeIngestionJobDO job : enterpriseRepository.findStaleJobs(staleBefore)) {
            job.setJobStatus("PENDING");
            job.setStage("RECOVERED");
            job.setErrorMessage("应用重启后恢复");
            enterpriseRepository.updateJob(job);
        }
    }

    @Scheduled(fixedDelayString = "${shiyu.knowledge.task.poll-interval:1000}")
    void poll() {
        int capacity = Math.max(0, concurrency - inFlight.size());
        if (capacity == 0) return;
        for (KnowledgeIngestionJobDO job : enterpriseRepository.pollPendingJobs(capacity)) {
            if (inFlight.add(job.getId())) {
                executor.submit(() -> execute(job.getId()));
            }
        }
    }

    private void execute(Long jobId) {
        KnowledgeIngestionJobDO job = enterpriseRepository.findJob(jobId);
        if (job == null) {
            inFlight.remove(jobId);
            return;
        }
        try {
            markRunning(job);
            KnowledgeDocumentBO document = documentRepository.selectById(job.getDocumentId());
            KnowledgeDocumentVersionDO version = enterpriseRepository.findVersion(job.getVersionId());
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
            ingestionService.ingest(job.getSpaceId(), job.getDocumentId(), job.getVersionId(),
                    parsed.text(), List.of());

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

    private void markRunning(KnowledgeIngestionJobDO job) {
        job.setJobStatus("RUNNING");
        job.setStage("STARTING");
        job.setProgress(1);
        job.setAttempts((job.getAttempts() == null ? 0 : job.getAttempts()) + 1);
        job.setStartedTime(LocalDateTime.now());
        job.setHeartbeatTime(LocalDateTime.now());
        enterpriseRepository.updateJob(job);
    }

    private void update(KnowledgeIngestionJobDO job, String stage, int progress) {
        job.setStage(stage);
        job.setProgress(progress);
        job.setHeartbeatTime(LocalDateTime.now());
        enterpriseRepository.updateJob(job);
    }

    private void fail(KnowledgeIngestionJobDO job, Exception exception) {
        log.error("Knowledge ingestion failed, jobId={}", job.getId(), exception);
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
        return parsers.stream().filter(parser ->
                parser.getSupportedFormat().equalsIgnoreCase(normalized)).findFirst();
    }

    private String limit(String value, int max) {
        if (value == null) return "未知错误";
        return value.length() <= max ? value : value.substring(0, max);
    }

    @PreDestroy
    void shutdown() {
        if (executor != null) executor.shutdown();
    }
}
