package com.shiyu.ai.knowledge.task;

import com.shiyu.ai.common.storage.ContentSecurityScanner;
import com.shiyu.ai.common.storage.ObjectStorage;
import com.shiyu.ai.common.thread.api.ThreadPoolManager;
import com.shiyu.ai.knowledge.document.DocumentParser;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentVersionBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeIngestionJobBO;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.rag.DocumentIngestionService;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmbeddedIngestionWorkerTest {
    @Test
    void initializesRecoversStaleJobsAndPollsPendingJobs() throws Exception {
        KnowledgeEnterpriseRepository enterprise = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        ThreadPoolManager pools = mock(ThreadPoolManager.class);
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        ExecutorService executor = mock(ExecutorService.class);
        KnowledgeIngestionJobBO stale = job(1L, 7L, 8L, 9L, "RUNNING", 10, 3);
        when(enterprise.findStaleJobs(any())).thenReturn(List.of(stale));
        when(pools.getExecutor("knowledge-ingestion")).thenReturn(executor);
        when(scheduler.scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any())).thenReturn(null);
        EmbeddedIngestionWorker worker = worker(enterprise, documents, mock(DocumentIngestionService.class),
                mock(ObjectStorage.class), mock(ContentSecurityScanner.class), List.of(), scheduler, pools);
        set(worker, "concurrency", 2);
        set(worker, "pollDelayMs", 0L);
        worker.initialize();
        assertEquals("PENDING", stale.getJobStatus());
        assertEquals("RECOVERED", stale.getStage());
        verify(enterprise).updateJob(new TenantId(10L), stale);
        worker.initialize();
        verify(pools, times(1)).getExecutor("knowledge-ingestion");
    }

    @Test
    void pollHonorsCapacityAndClaimsOnlyJobsWithValidTenant() throws Exception {
        KnowledgeEnterpriseRepository enterprise = mock(KnowledgeEnterpriseRepository.class);
        ExecutorService executor = mock(ExecutorService.class);
        KnowledgeIngestionJobBO pending = job(5L, 7L, 8L, 9L, "PENDING", 0, 2);
        when(enterprise.pollPendingJobs(2)).thenReturn(List.of(pending));
        EmbeddedIngestionWorker worker = worker(enterprise, mock(KnowledgeDocumentRepository.class),
                mock(DocumentIngestionService.class), mock(ObjectStorage.class), mock(ContentSecurityScanner.class),
                List.of(), mock(ScheduledExecutorService.class), mock(ThreadPoolManager.class));
        set(worker, "executor", executor);
        set(worker, "concurrency", 2);
        Method poll = EmbeddedIngestionWorker.class.getDeclaredMethod("poll");
        poll.setAccessible(true);
        poll.invoke(worker);
        verify(enterprise).pollPendingJobs(2);
        verify(executor).submit(any(Runnable.class));
        set(worker, "concurrency", 0);
        poll.invoke(worker);
        verify(enterprise, times(1)).pollPendingJobs(2);
    }

    @Test
    void executesSuccessfulJobWithTenantScopedDependencies() throws Exception {
        KnowledgeEnterpriseRepository enterprise = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        DocumentIngestionService ingestion = mock(DocumentIngestionService.class);
        ObjectStorage storage = mock(ObjectStorage.class);
        ContentSecurityScanner scanner = mock(ContentSecurityScanner.class);
        DocumentParser parser = mock(DocumentParser.class);
        KnowledgeIngestionJobBO job = job(2L, 7L, 8L, 9L, "PENDING", 0, 3);
        KnowledgeDocumentBO document = document(8L, "TXT");
        KnowledgeDocumentVersionBO version = version(9L, "obj");
        when(enterprise.findJob(new TenantId(10L), 2L)).thenReturn(job);
        when(documents.selectById(new TenantId(10L), 8L)).thenReturn(document);
        when(enterprise.findVersion(new TenantId(10L), 9L)).thenReturn(version);
        when(parser.getSupportedFormat()).thenReturn("txt");
        when(parser.parse(any(byte[].class))).thenReturn(new DocumentParser.ParseResult("parsed", "hello", ""));
        when(storage.open("obj")).thenReturn(new ObjectStorage.ReadableObject(new ByteArrayInputStream("hello".getBytes()), "a.txt", "text/plain", 5));
        EmbeddedIngestionWorker worker = worker(enterprise, documents, ingestion, storage, scanner,
                List.of(parser), mock(ScheduledExecutorService.class), mock(ThreadPoolManager.class));

        invokeExecute(worker, new TenantId(10L), 2L);
        assertEquals("SUCCEEDED", job.getJobStatus());
        assertEquals("READY", document.getParseStatus());
        assertEquals("READY", version.getParseStatus());
        verify(ingestion).ingest(any(com.shiyu.ai.kernel.context.ActorContext.class), eq(7L), eq(8L), eq(9L), eq("hello"), eq(List.of()));
        verify(documents).selectById(new TenantId(10L), 8L);
    }

    @Test
    void reusesStoredContentWhenObjectIsMissingAndFallsBackToTextParser() throws Exception {
        KnowledgeEnterpriseRepository enterprise = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        DocumentIngestionService ingestion = mock(DocumentIngestionService.class);
        ObjectStorage storage = mock(ObjectStorage.class);
        DocumentParser textParser = mock(DocumentParser.class);
        KnowledgeIngestionJobBO job = job(6L, 7L, 8L, 9L, "PENDING", 0, 1);
        KnowledgeDocumentBO document = document(8L, "REFERENCE");
        KnowledgeDocumentVersionBO version = version(9L, "missing");
        version.setContent("stored text");
        when(enterprise.findJob(new TenantId(10L), 6L)).thenReturn(job);
        when(documents.selectById(new TenantId(10L), 8L)).thenReturn(document);
        when(enterprise.findVersion(new TenantId(10L), 9L)).thenReturn(version);
        when(textParser.getSupportedFormat()).thenReturn("txt");
        when(storage.open("missing")).thenThrow(new java.io.IOException("gone"));
        EmbeddedIngestionWorker worker = worker(enterprise, documents, ingestion, storage,
                mock(ContentSecurityScanner.class), List.of(textParser), mock(ScheduledExecutorService.class), mock(ThreadPoolManager.class));
        invokeExecute(worker, new TenantId(10L), 6L);
        assertEquals("SUCCEEDED", job.getJobStatus());
        verify(textParser, never()).parse(any(byte[].class));
        verify(ingestion).ingest(any(com.shiyu.ai.kernel.context.ActorContext.class), eq(7L), eq(8L), eq(9L), eq("stored text"), eq(List.of()));
    }

    @Test
    void retriesStorageFailureThenMarksFinalFailureAndHonorsCancellation() throws Exception {
        KnowledgeEnterpriseRepository enterprise = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        ObjectStorage storage = mock(ObjectStorage.class);
        DocumentParser parser = mock(DocumentParser.class);
        KnowledgeDocumentBO document = document(8L, "TXT");
        KnowledgeDocumentVersionBO version = version(9L, "missing");
        KnowledgeIngestionJobBO retry = job(3L, 7L, 8L, 9L, "PENDING", 1, 3);
        when(enterprise.findJob(new TenantId(10L), 3L)).thenReturn(retry);
        when(documents.selectById(new TenantId(10L), 8L)).thenReturn(document);
        when(enterprise.findVersion(new TenantId(10L), 9L)).thenReturn(version);
        when(parser.getSupportedFormat()).thenReturn("txt");
        when(storage.open("missing")).thenThrow(new java.io.IOException("gone"));
        EmbeddedIngestionWorker worker = worker(enterprise, documents, mock(DocumentIngestionService.class), storage,
                mock(ContentSecurityScanner.class), List.of(parser), mock(ScheduledExecutorService.class), mock(ThreadPoolManager.class));
        invokeExecute(worker, new TenantId(10L), 3L);
        assertEquals("PENDING", retry.getJobStatus());
        assertEquals("RETRY_WAIT", retry.getStage());
        retry.setAttempts(3);
        retry.setJobStatus("PENDING");
        invokeExecute(worker, new TenantId(10L), 3L);
        assertEquals("FAILED", retry.getJobStatus());
        assertEquals("FAILED", document.getParseStatus());

        clearInvocations(storage);
        KnowledgeIngestionJobBO cancelled = job(4L, 7L, 8L, 9L, "CANCELLED", 1, 3);
        when(enterprise.findJob(new TenantId(10L), 4L)).thenReturn(cancelled);
        invokeExecute(worker, new TenantId(10L), 4L);
        verify(storage, never()).open(anyString());
    }

    @Test
    void skipsMissingOrAlreadyRunningJobsAndMarksMissingDocumentsFailed() throws Exception {
        KnowledgeEnterpriseRepository enterprise = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        EmbeddedIngestionWorker worker = worker(enterprise, documents,
                mock(DocumentIngestionService.class), mock(ObjectStorage.class),
                mock(ContentSecurityScanner.class), List.of(),
                mock(ScheduledExecutorService.class), mock(ThreadPoolManager.class));

        when(enterprise.findJob(new TenantId(10L), 99L)).thenReturn(null);
        invokeExecute(worker, new TenantId(10L), 99L);

        KnowledgeIngestionJobBO running = job(100L, 7L, 8L, 9L, "RUNNING", 1, 2);
        when(enterprise.findJob(new TenantId(10L), 100L)).thenReturn(running);
        invokeExecute(worker, new TenantId(10L), 100L);
        verify(documents, never()).selectById(any(TenantId.class), anyLong());

        KnowledgeIngestionJobBO missing = job(101L, 7L, 8L, 9L, "PENDING", 2, 2);
        when(enterprise.findJob(new TenantId(10L), 101L)).thenReturn(missing);
        when(documents.selectById(new TenantId(10L), 8L)).thenReturn(null);
        invokeExecute(worker, new TenantId(10L), 101L);
        assertEquals("FAILED", missing.getJobStatus());
        assertEquals("FAILED", missing.getStage());
        verify(enterprise, atLeastOnce()).updateJob(new TenantId(10L), missing);
    }

    @Test
    void handlesBlankParserOutputAndCancellationAfterEmbedding() throws Exception {
        KnowledgeEnterpriseRepository enterprise = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        DocumentIngestionService ingestion = mock(DocumentIngestionService.class);
        ObjectStorage storage = mock(ObjectStorage.class);
        DocumentParser parser = mock(DocumentParser.class);
        KnowledgeIngestionJobBO blank = job(102L, 7L, 8L, 9L, "PENDING", 0, 1);
        KnowledgeDocumentBO document = document(8L, "markdown");
        KnowledgeDocumentVersionBO version = version(9L, "obj");
        when(enterprise.findJob(new TenantId(10L), 102L)).thenReturn(blank);
        when(documents.selectById(new TenantId(10L), 8L)).thenReturn(document);
        when(enterprise.findVersion(new TenantId(10L), 9L)).thenReturn(version);
        when(parser.getSupportedFormat()).thenReturn("md");
        when(parser.parse(any(byte[].class))).thenReturn(new DocumentParser.ParseResult(null, "  ", ""));
        when(storage.open("obj")).thenReturn(new ObjectStorage.ReadableObject(
                new ByteArrayInputStream("raw".getBytes()), "a.md", "text/markdown", 3));
        EmbeddedIngestionWorker worker = worker(enterprise, documents, ingestion, storage,
                mock(ContentSecurityScanner.class), List.of(parser),
                mock(ScheduledExecutorService.class), mock(ThreadPoolManager.class));
        invokeExecute(worker, new TenantId(10L), 102L);
        assertEquals("FAILED", blank.getJobStatus());
        verify(ingestion, never()).ingest(any(com.shiyu.ai.kernel.context.ActorContext.class),
                anyLong(), anyLong(), anyLong(), anyString(), anyList());

        KnowledgeIngestionJobBO cancelled = job(103L, 7L, 8L, 9L, "PENDING", 0, 1);
        when(enterprise.findJob(new TenantId(10L), 103L)).thenReturn(cancelled);
        doAnswer(invocation -> {
            cancelled.setJobStatus("CANCELLED");
            return null;
        }).when(ingestion).ingest(any(com.shiyu.ai.kernel.context.ActorContext.class),
                anyLong(), anyLong(), anyLong(), anyString(), anyList());
        when(parser.parse(any(byte[].class))).thenReturn(new DocumentParser.ParseResult("title", "text", ""));
        invokeExecute(worker, new TenantId(10L), 103L);
        assertEquals("CANCELLED", cancelled.getJobStatus());
        verify(ingestion).delete(new TenantId(10L), 8L);
    }

    @Test
    void coversPollingAndExecutionBoundaryBranches() throws Exception {
        KnowledgeEnterpriseRepository enterprise = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        DocumentIngestionService ingestion = mock(DocumentIngestionService.class);
        ObjectStorage storage = mock(ObjectStorage.class);
        ContentSecurityScanner scanner = mock(ContentSecurityScanner.class);
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        ExecutorService executor = mock(ExecutorService.class);
        ThreadPoolManager pools = mock(ThreadPoolManager.class);
        EmbeddedIngestionWorker worker = worker(enterprise, documents, ingestion, storage, scanner,
                List.of(), scheduler, pools);

        // A full in-flight set must short-circuit without polling; an empty result is a no-op.
        set(worker, "executor", executor);
        set(worker, "concurrency", 1);
        Field inFlight = EmbeddedIngestionWorker.class.getDeclaredField("inFlight");
        inFlight.setAccessible(true);
        @SuppressWarnings("unchecked") Set<Long> ids = (Set<Long>) inFlight.get(worker);
        ids.add(500L);
        worker.poll();
        verify(enterprise, never()).pollPendingJobs(anyInt());
        ids.clear();
        when(enterprise.pollPendingJobs(1)).thenReturn(List.of());
        worker.poll();
        verify(enterprise).pollPendingJobs(1);

        // Cancellation after the claim is observed before reading any source object.
        KnowledgeIngestionJobBO claimed = job(110L, 7L, 8L, 9L, "PENDING", 0, 1);
        KnowledgeIngestionJobBO cancelled = job(110L, 7L, 8L, 9L, "CANCELLED", 0, 1);
        when(enterprise.findJob(new TenantId(10L), 110L)).thenReturn(claimed, claimed, cancelled);
        invokeExecute(worker, new TenantId(10L), 110L);
        verify(storage, never()).open(anyString());

        // Missing source content is a hard failure, while an unknown type without a text
        // parser also fails explicitly instead of silently succeeding.
        KnowledgeIngestionJobBO noContent = job(111L, 7L, 8L, 9L, "PENDING", 0, 1);
        KnowledgeDocumentBO document = document(8L, "UNKNOWN");
        KnowledgeDocumentVersionBO version = version(9L, "missing");
        when(enterprise.findJob(new TenantId(10L), 111L)).thenReturn(noContent);
        when(documents.selectById(new TenantId(10L), 8L)).thenReturn(document);
        when(enterprise.findVersion(new TenantId(10L), 9L)).thenReturn(version);
        when(storage.open("missing")).thenThrow(new java.io.IOException("gone"));
        invokeExecute(worker, new TenantId(10L), 111L);
        assertEquals("FAILED", noContent.getJobStatus());

        KnowledgeIngestionJobBO unknown = job(112L, 7L, 8L, 9L, "PENDING", 0, 1);
        KnowledgeDocumentVersionBO readable = version(9L, "obj");
        when(enterprise.findJob(new TenantId(10L), 112L)).thenReturn(unknown);
        when(enterprise.findVersion(new TenantId(10L), 9L)).thenReturn(readable);
        when(storage.open("obj")).thenReturn(new ObjectStorage.ReadableObject(
                new ByteArrayInputStream("raw".getBytes()), "a.bin", "application/octet-stream", 3));
        invokeExecute(worker, new TenantId(10L), 112L);
        assertEquals("FAILED", unknown.getJobStatus());

        // An unattributed job is rejected after parsing and before embedding.
        DocumentParser parser = mock(DocumentParser.class);
        when(parser.getSupportedFormat()).thenReturn("txt");
        when(parser.parse(any(byte[].class))).thenReturn(new DocumentParser.ParseResult("title", "text", ""));
        KnowledgeIngestionJobBO noActor = job(113L, 7L, 8L, 9L, "PENDING", 0, 1);
        noActor.setActorUserId(0L);
        when(enterprise.findJob(new TenantId(10L), 113L)).thenReturn(noActor);
        when(storage.open("obj")).thenReturn(new ObjectStorage.ReadableObject(
                new ByteArrayInputStream("raw".getBytes()), "a.txt", "text/plain", 3));
        EmbeddedIngestionWorker actorWorker = worker(enterprise, documents, ingestion, storage, scanner,
                List.of(parser), scheduler, pools);
        invokeExecute(actorWorker, new TenantId(10L), 113L);
        assertEquals("FAILED", noActor.getJobStatus());
        verify(ingestion, never()).ingest(any(com.shiyu.ai.kernel.context.ActorContext.class),
                anyLong(), anyLong(), anyLong(), anyString(), anyList());

        // Successful parsing populates a missing version title and shutdown is idempotent.
        KnowledgeIngestionJobBO titled = job(114L, 7L, 8L, 9L, "PENDING", 0, 1);
        KnowledgeDocumentVersionBO untitled = version(9L, "obj");
        when(enterprise.findJob(new TenantId(10L), 114L)).thenReturn(titled);
        when(enterprise.findVersion(new TenantId(10L), 9L)).thenReturn(untitled);
        when(parser.parse(any(byte[].class))).thenReturn(new DocumentParser.ParseResult("Parsed title", "text", ""));
        invokeExecute(actorWorker, new TenantId(10L), 114L);
        assertEquals("Parsed title", untitled.getTitle());
        worker.shutdown();
        worker.shutdown();
    }

    @Test
    void coversWorkerUtilityAndDuplicateClaimBranches() throws Exception {
        KnowledgeEnterpriseRepository enterprise = mock(KnowledgeEnterpriseRepository.class);
        ExecutorService executor = mock(ExecutorService.class);
        KnowledgeIngestionJobBO duplicate = job(200L, 7L, 8L, 9L, "PENDING", 0, 1);
        when(enterprise.pollPendingJobs(3)).thenReturn(List.of(duplicate, duplicate));
        EmbeddedIngestionWorker worker = worker(enterprise, mock(KnowledgeDocumentRepository.class),
                mock(DocumentIngestionService.class), mock(ObjectStorage.class),
                mock(ContentSecurityScanner.class), List.of(), mock(ScheduledExecutorService.class),
                mock(ThreadPoolManager.class));
        set(worker, "executor", executor);
        set(worker, "concurrency", 3);
        invoke(worker, "poll", new Class<?>[0]);
        verify(executor, times(1)).submit(any(Runnable.class));

        Method jobTenant = EmbeddedIngestionWorker.class.getDeclaredMethod("jobTenant", KnowledgeIngestionJobBO.class);
        jobTenant.setAccessible(true);
        assertThrows(Exception.class, () -> jobTenant.invoke(worker, new Object[]{null}));
        KnowledgeIngestionJobBO invalidTenant = job(201L, 7L, 8L, 9L, "PENDING", 0, 1);
        invalidTenant.setTenantId(0L);
        assertThrows(Exception.class, () -> jobTenant.invoke(worker, invalidTenant));

        Method parserFor = EmbeddedIngestionWorker.class.getDeclaredMethod("parserFor", String.class);
        parserFor.setAccessible(true);
        assertTrue(((java.util.Optional<?>) parserFor.invoke(worker, new Object[]{null})).isEmpty());
        Method limit = EmbeddedIngestionWorker.class.getDeclaredMethod("limit", String.class, int.class);
        limit.setAccessible(true);
        assertEquals("未知错误", limit.invoke(worker, new Object[]{null, 3}));
        assertEquals("abc", limit.invoke(worker, "abcdef", 3));

        Method markRunning = EmbeddedIngestionWorker.class.getDeclaredMethod(
                "markRunning", TenantId.class, KnowledgeIngestionJobBO.class);
        markRunning.setAccessible(true);
        when(enterprise.findJob(new TenantId(10L), 202L)).thenReturn(null);
        KnowledgeIngestionJobBO missing = job(202L, 7L, 8L, 9L, "PENDING", 0, 1);
        assertNull(markRunning.invoke(worker, new TenantId(10L), missing));
    }

    private static EmbeddedIngestionWorker worker(KnowledgeEnterpriseRepository enterprise,
                                                   KnowledgeDocumentRepository documents,
                                                   DocumentIngestionService ingestion,
                                                   ObjectStorage storage,
                                                   ContentSecurityScanner scanner,
                                                   List<DocumentParser> parsers,
                                                   ScheduledExecutorService scheduler,
                                                   ThreadPoolManager pools) {
        return new EmbeddedIngestionWorker(enterprise, documents, ingestion, storage, scanner, parsers, scheduler, pools);
    }

    private static void invokeExecute(EmbeddedIngestionWorker worker, TenantId tenant, Long id) throws Exception {
        Method method = EmbeddedIngestionWorker.class.getDeclaredMethod("execute", TenantId.class, Long.class);
        method.setAccessible(true);
        method.invoke(worker, tenant, id);
    }

    private static Object invoke(EmbeddedIngestionWorker worker, String name, Class<?>[] types, Object... args) throws Exception {
        Method method = EmbeddedIngestionWorker.class.getDeclaredMethod(name, types);
        method.setAccessible(true);
        return method.invoke(worker, args);
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name); field.setAccessible(true); field.set(target, value);
    }

    private static KnowledgeIngestionJobBO job(Long id, Long space, Long document, Long version, String status, int attempts, int max) {
        KnowledgeIngestionJobBO value = new KnowledgeIngestionJobBO(); value.setId(id); value.setTenantId(10L); value.setSpaceId(space); value.setDocumentId(document); value.setVersionId(version); value.setActorUserId(20L); value.setJobStatus(status); value.setAttempts(attempts); value.setMaxAttempts(max); return value;
    }

    private static KnowledgeDocumentBO document(Long id, String type) { KnowledgeDocumentBO value = new KnowledgeDocumentBO(); value.setId(id); value.setTenantId(10L); value.setSpaceId(7L); value.setDocType(type); return value; }
    private static KnowledgeDocumentVersionBO version(Long id, String key) { KnowledgeDocumentVersionBO value = new KnowledgeDocumentVersionBO(); value.setId(id); value.setTenantId(10L); value.setObjectKey(key); return value; }
}
