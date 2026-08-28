package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.tx.TransactionTemplateExecutor;
import com.shiyu.ai.common.storage.ObjectStorage;
import com.shiyu.ai.common.storage.StorageMetadataStore;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentVersionBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeIngestionJobBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeSpaceBO;
import com.shiyu.ai.common.storage.StorageMetadataStore;
import com.shiyu.ai.common.storage.ObjectStorage;
import com.shiyu.ai.common.core.tx.TransactionTemplateExecutor;
import com.shiyu.ai.knowledge.index.KnowledgeIndexService;
import com.shiyu.ai.knowledge.service.KnowledgeDocumentRelationService;
import com.shiyu.ai.knowledge.rag.DocumentIngestionService;
import com.shiyu.ai.knowledge.index.KnowledgeIndexService;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.rag.DocumentIngestionService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.RoleId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.knowledge.service.KnowledgeAuditService;
import com.shiyu.ai.knowledge.service.KnowledgeDocumentRelationService;
import com.shiyu.ai.knowledge.service.EnterpriseDocumentService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;

import java.util.List;

class EnterpriseDocumentServiceImplTenantTest {

    @Test
    void rejectsDocumentOwnedByAnotherTenantBeforePermissionLookup() {
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeDocumentBO document = new KnowledgeDocumentBO();
        document.setId(99L);
        document.setTenantId(2L);
        document.setSpaceId(5L);
        when(documents.selectById(new TenantId(1L), 99L)).thenReturn(document);

        EnterpriseDocumentServiceImpl service = new EnterpriseDocumentServiceImpl(
                documents,
                mock(KnowledgeEnterpriseRepository.class),
                spaces,
                mock(KnowledgeAuditService.class),
                mock(KnowledgeDocumentRelationService.class),
                mock(DocumentIngestionService.class),
                mock(KnowledgeIndexService.class),
                mock(TransactionTemplateExecutor.class),
                mock(StorageMetadataStore.class),
                mock(ObjectStorage.class));

        ActorContext actor = new ActorContext(
                new TenantId(1L), new UserId(7L), new RoleId(3L), false);

        assertThrows(ServiceException.class, () -> service.get(actor, 99L));
        verifyNoInteractions(spaces);
    }

    @Test
    void pagesDocumentsThroughTheSpacePermissionBoundary() {
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeDocumentBO document = new KnowledgeDocumentBO(); document.setId(10L); document.setSpaceId(5L); document.setTitle("Guide");
        when(documents.pageBySpace(new TenantId(1L), 5L, 1, 20, null, null, null))
                .thenReturn(new PageData<>(List.of(document), 1));
        EnterpriseDocumentServiceImpl service = new EnterpriseDocumentServiceImpl(documents,
                mock(KnowledgeEnterpriseRepository.class), spaces, mock(KnowledgeAuditService.class),
                mock(KnowledgeDocumentRelationService.class), mock(DocumentIngestionService.class),
                mock(KnowledgeIndexService.class), mock(TransactionTemplateExecutor.class),
                mock(StorageMetadataStore.class), mock(ObjectStorage.class));
        var result = service.page(actor(1L), 5L, 1, 20, null, null, null);
        assertEquals(1, result.getItems().size());
        verify(spaces).requireAccess(5L, KnowledgeSpaceService.SpaceRole.VIEWER, actor(1L));
    }

    @Test
    void returnsExistingDocumentForIdempotentStoredFileRegistration() {
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeDocumentBO duplicate = new KnowledgeDocumentBO(); duplicate.setId(10L); duplicate.setSpaceId(5L); duplicate.setTitle("Guide"); duplicate.setCurrentVersionId(3L);
        when(documents.findBySpaceAndChecksum(new TenantId(1L), 5L, "sha")).thenReturn(duplicate);
        EnterpriseDocumentServiceImpl service = new EnterpriseDocumentServiceImpl(documents,
                mock(KnowledgeEnterpriseRepository.class), spaces, mock(KnowledgeAuditService.class),
                mock(KnowledgeDocumentRelationService.class), mock(DocumentIngestionService.class),
                mock(KnowledgeIndexService.class), mock(TransactionTemplateExecutor.class),
                mock(StorageMetadataStore.class), mock(ObjectStorage.class));
        var request = new EnterpriseDocumentService.StoredFileRequest(5L, "Guide", "guide.pdf", "objects/guide",
                null, "application/pdf", 42L, "sha");
        var result = service.registerStoredFile(actor(1L), request);
        assertTrue(result.duplicate());
        assertEquals(3L, result.versionId());
        verify(documents, never()).insert(any(), any());
    }

    @Test
    void registersNewStoredFileWithVersionAndIngestionJob() {
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeEnterpriseRepository enterprise = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeAuditService audit = mock(KnowledgeAuditService.class);
        StorageMetadataStore metadata = mock(StorageMetadataStore.class);
        KnowledgeSpaceBO space = new KnowledgeSpaceBO(); space.setId(5L); space.setTenantId(1L); space.setEmbeddingProfile("embed-v1");
        when(enterprise.findSpace(new TenantId(1L), 5L)).thenReturn(space);
        when(metadata.findObjectByKey(1L, "objects/guide")).thenReturn(java.util.Optional.empty());
        when(documents.findBySpaceAndChecksum(new TenantId(1L), 5L, "sha-new")).thenReturn(null);
        doAnswer(invocation -> { KnowledgeDocumentBO value = invocation.getArgument(1); value.setId(20L); return 1; }).when(documents).insert(eq(new TenantId(1L)), any(KnowledgeDocumentBO.class));
        doAnswer(invocation -> { KnowledgeDocumentVersionBO value = invocation.getArgument(1); value.setId(21L); return null; }).when(enterprise).insertVersion(eq(new TenantId(1L)), any(KnowledgeDocumentVersionBO.class));
        when(enterprise.findJobByKey(new TenantId(1L), "INGEST:5:sha-new")).thenReturn(null);
        doAnswer(invocation -> { com.shiyu.ai.knowledge.domain.model.KnowledgeIngestionJobBO value = invocation.getArgument(1); value.setId(22L); return null; }).when(enterprise).insertJob(eq(new TenantId(1L)), any());
        EnterpriseDocumentServiceImpl service = new EnterpriseDocumentServiceImpl(documents, enterprise, spaces, audit,
                mock(KnowledgeDocumentRelationService.class), mock(DocumentIngestionService.class), mock(KnowledgeIndexService.class),
                mock(TransactionTemplateExecutor.class), metadata, mock(ObjectStorage.class));
        var request = new EnterpriseDocumentService.StoredFileRequest(5L, "Guide", "guide.PDF", "objects/guide", null, "application/pdf", 42L, "sha-new");
        var result = service.registerStoredFile(actor(1L), request);
        assertEquals(20L, result.document().id());
        assertEquals(21L, result.versionId());
        assertEquals(22L, result.jobId());
        assertTrue(result.document().docType().equals("pdf"));
        verify(audit).record(actor(1L), 5L, "DOCUMENT", 20L, "UPLOAD", request);
    }

    @Test
    void failsStoredFileRegistrationWhenDocumentInsertAffectsNoRows() {
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        StorageMetadataStore metadata = mock(StorageMetadataStore.class);
        when(documents.findBySpaceAndChecksum(new TenantId(1L), 5L, "sha-failed")).thenReturn(null);
        when(metadata.findObjectByKey(1L, "objects/failed")).thenReturn(java.util.Optional.empty());
        when(documents.insert(eq(new TenantId(1L)), any(KnowledgeDocumentBO.class))).thenReturn(0);
        EnterpriseDocumentServiceImpl service = new EnterpriseDocumentServiceImpl(documents,
                mock(KnowledgeEnterpriseRepository.class), spaces, mock(KnowledgeAuditService.class),
                mock(KnowledgeDocumentRelationService.class), mock(DocumentIngestionService.class),
                mock(KnowledgeIndexService.class), mock(TransactionTemplateExecutor.class), metadata,
                mock(ObjectStorage.class));

        var request = new EnterpriseDocumentService.StoredFileRequest(5L, "Failed", "failed.txt",
                "objects/failed", null, "text/plain", 1L, "sha-failed");
        assertThrows(ServiceException.class, () -> service.registerStoredFile(actor(1L), request));
    }

    @Test
    void transitionsDraftAndRollsBackToANewVersion() {
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeEnterpriseRepository enterprise = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeAuditService audit = mock(KnowledgeAuditService.class);
        KnowledgeDocumentBO document = document(10L, 5L, 9L, "DRAFT", "READY");
        KnowledgeDocumentVersionBO version = version(9L, 10L, 1, "DRAFT", "READY");
        when(documents.selectById(new TenantId(1L), 10L)).thenReturn(document);
        when(enterprise.findVersion(new TenantId(1L), 9L)).thenReturn(version);
        when(enterprise.nextVersionNo(new TenantId(1L), 10L)).thenReturn(2);
        when(enterprise.insertVersion(eq(new TenantId(1L)), any(KnowledgeDocumentVersionBO.class))).thenAnswer(invocation -> {
            KnowledgeDocumentVersionBO value = invocation.getArgument(1); value.setId(11L); return value;
        });
        EnterpriseDocumentServiceImpl service = new EnterpriseDocumentServiceImpl(documents, enterprise, spaces, audit,
                mock(KnowledgeDocumentRelationService.class), mock(DocumentIngestionService.class), mock(KnowledgeIndexService.class),
                mock(TransactionTemplateExecutor.class), mock(StorageMetadataStore.class), mock(ObjectStorage.class));

        assertEquals("REVIEWING", service.submit(actor(1L), 10L, "please review").lifecycleStatus());
        document.setLifecycleStatus("REVIEWING");
        assertEquals("DRAFT", service.reject(actor(1L), 10L, "needs changes").lifecycleStatus());
        document.setLifecycleStatus("DRAFT");
        assertEquals(11L, service.rollback(actor(1L), 10L, 9L).currentVersionId());
        verify(enterprise, atLeast(2)).insertReview(eq(new TenantId(1L)), any());
    }

    @Test
    void rejectsInvalidPublishAndDeletesDocumentAfterTransaction() throws Exception {
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeEnterpriseRepository enterprise = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeAuditService audit = mock(KnowledgeAuditService.class);
        KnowledgeDocumentRelationService relations = mock(KnowledgeDocumentRelationService.class);
        DocumentIngestionService ingestion = mock(DocumentIngestionService.class);
        KnowledgeIndexService index = mock(KnowledgeIndexService.class);
        TransactionTemplateExecutor tx = mock(TransactionTemplateExecutor.class);
        StorageMetadataStore metadata = mock(StorageMetadataStore.class);
        ObjectStorage storage = mock(ObjectStorage.class);
        KnowledgeDocumentBO document = document(10L, 5L, 9L, "DRAFT", "PENDING"); document.setTenantId(1L); document.setObjectKey("objects/doc");
        when(documents.selectById(new TenantId(1L), 10L)).thenReturn(document);
        when(enterprise.findSpace(new TenantId(1L), 5L)).thenReturn(new KnowledgeSpaceBO());
        EnterpriseDocumentServiceImpl service = new EnterpriseDocumentServiceImpl(documents, enterprise, spaces, audit, relations, ingestion, index, tx, metadata, storage);
        assertThrows(ServiceException.class, () -> service.publish(actor(1L), 10L, "publish"));
        when(tx.execute(any(), any())).thenAnswer(invocation -> ((java.util.function.Supplier<?>) invocation.getArgument(1)).get());
        service.delete(actor(1L), 10L);
        verify(relations).removeDocumentRelations(actor(1L), 10L);
        verify(documents).deleteById(new TenantId(1L), 10L);
        verify(storage).delete("objects/doc");
        verify(index).rebuild(new TenantId(1L), 5L);
    }

    @Test
    void rebindsDeletedIdempotencyJobAndUsesReviewerForRequiredPublish() {
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeEnterpriseRepository enterprise = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeAuditService audit = mock(KnowledgeAuditService.class);
        StorageMetadataStore metadata = mock(StorageMetadataStore.class);
        KnowledgeDocumentBO previous = new KnowledgeDocumentBO();
        previous.setId(99L);
        previous.setDelFlag(1);
        KnowledgeIngestionJobBO existingJob = new KnowledgeIngestionJobBO();
        existingJob.setId(33L);
        existingJob.setDocumentId(99L);
        existingJob.setVersionId(98L);
        when(documents.findBySpaceAndChecksum(new TenantId(1L), 5L, "sha-rebind")).thenReturn(null);
        when(documents.selectById(new TenantId(1L), 99L)).thenReturn(previous);
        when(enterprise.findJobByKey(new TenantId(1L), "INGEST:5:sha-rebind")).thenReturn(existingJob);
        when(metadata.findObjectByKey(1L, "objects/rebind")).thenReturn(java.util.Optional.empty());
        KnowledgeSpaceBO space = new KnowledgeSpaceBO();
        space.setId(5L);
        space.setTenantId(1L);
        space.setBindingMode("REQUIRED");
        space.setReviewMode("REQUIRED");
        when(enterprise.findSpace(new TenantId(1L), 5L)).thenReturn(space);
        doAnswer(invocation -> { ((KnowledgeDocumentBO) invocation.getArgument(1)).setId(20L); return 1; })
                .when(documents).insert(eq(new TenantId(1L)), any(KnowledgeDocumentBO.class));
        doAnswer(invocation -> { ((KnowledgeDocumentVersionBO) invocation.getArgument(1)).setId(21L); return null; })
                .when(enterprise).insertVersion(eq(new TenantId(1L)), any(KnowledgeDocumentVersionBO.class));

        EnterpriseDocumentServiceImpl service = new EnterpriseDocumentServiceImpl(documents, enterprise, spaces, audit,
                mock(KnowledgeDocumentRelationService.class), mock(DocumentIngestionService.class),
                mock(KnowledgeIndexService.class), mock(TransactionTemplateExecutor.class), metadata, mock(ObjectStorage.class));
        var request = new EnterpriseDocumentService.StoredFileRequest(5L, "Guide", "guide.txt", "objects/rebind",
                "", "text/plain", 3L, "sha-rebind");
        var result = service.registerStoredFile(actor(1L), request);
        assertEquals(33L, result.jobId());
        assertTrue(!result.duplicate());
        verify(enterprise).updateJob(eq(new TenantId(1L)), eq(existingJob));

    }

    @Test
    void rejectsInvalidTransitionsAndStorageCleanupFailuresAreDeferred() throws Exception {
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeEnterpriseRepository enterprise = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeDocumentBO document = document(10L, 5L, 9L, "DRAFT", "READY");
        when(documents.selectById(new TenantId(1L), 10L)).thenReturn(document);
        EnterpriseDocumentServiceImpl service = new EnterpriseDocumentServiceImpl(documents, enterprise, spaces,
                mock(KnowledgeAuditService.class), mock(KnowledgeDocumentRelationService.class),
                mock(DocumentIngestionService.class), mock(KnowledgeIndexService.class), mock(TransactionTemplateExecutor.class),
                mock(StorageMetadataStore.class), mock(ObjectStorage.class));
        assertThrows(ServiceException.class, () -> service.approve(actor(1L), 10L, "approve"));
        assertThrows(ServiceException.class, () -> service.rollback(actor(1L), 10L, 999L));
    }

    @Test
    void publishesReadyDocumentWithRequiredBindingAndReviewRole() {
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeEnterpriseRepository enterprise = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeDocumentRelationService relations = mock(KnowledgeDocumentRelationService.class);
        KnowledgeDocumentBO document = document(10L, 5L, 9L, "REVIEWING", "READY");
        KnowledgeSpaceBO space = new KnowledgeSpaceBO();
        space.setId(5L);
        space.setTenantId(1L);
        space.setBindingMode("REQUIRED");
        space.setReviewMode("REQUIRED");
        when(documents.selectById(new TenantId(1L), 10L)).thenReturn(document);
        when(enterprise.findSpace(new TenantId(1L), 5L)).thenReturn(space);
        when(relations.listPointIds(actor(1L), 10L)).thenReturn(List.of(7L));
        when(enterprise.findVersion(new TenantId(1L), 9L)).thenReturn(version(9L, 10L, 1, "REVIEWING", "READY"));
        EnterpriseDocumentServiceImpl service = new EnterpriseDocumentServiceImpl(documents, enterprise, spaces,
                mock(KnowledgeAuditService.class), relations, mock(DocumentIngestionService.class),
                mock(KnowledgeIndexService.class), mock(TransactionTemplateExecutor.class),
                mock(StorageMetadataStore.class), mock(ObjectStorage.class));

        TransactionSynchronizationManager.initSynchronization();
        EnterpriseDocumentService.DocumentView result;
        try {
            result = service.publish(actor(1L), 10L, "publish");
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
        assertEquals("PUBLISHED", result.lifecycleStatus());
        verify(spaces).requireAccess(5L, KnowledgeSpaceService.SpaceRole.REVIEWER, actor(1L));
        verify(enterprise).insertReview(eq(new TenantId(1L)), any());
    }

    @Test
    void coversExistingJobWithoutPreviousDocumentRollbackArchiveAndCleanupFailures() throws Exception {
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeEnterpriseRepository enterprise = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeAuditService audit = mock(KnowledgeAuditService.class);
        KnowledgeDocumentRelationService relations = mock(KnowledgeDocumentRelationService.class);
        DocumentIngestionService ingestion = mock(DocumentIngestionService.class);
        KnowledgeIndexService index = mock(KnowledgeIndexService.class);
        TransactionTemplateExecutor tx = mock(TransactionTemplateExecutor.class);
        StorageMetadataStore metadata = mock(StorageMetadataStore.class);
        ObjectStorage storage = mock(ObjectStorage.class);
        ActorContext actor = actor(1L);

        KnowledgeIngestionJobBO existingJob = new KnowledgeIngestionJobBO();
        existingJob.setId(30L);
        existingJob.setDocumentId(null);
        when(documents.findBySpaceAndChecksum(new TenantId(1L), 5L, "sha-existing")).thenReturn(null);
        when(enterprise.findJobByKey(new TenantId(1L), "INGEST:5:sha-existing")).thenReturn(existingJob);
        when(metadata.findObjectByKey(1L, "objects/existing")).thenReturn(java.util.Optional.empty());
        doAnswer(invocation -> { ((KnowledgeDocumentBO) invocation.getArgument(1)).setId(31L); return 1; })
                .when(documents).insert(eq(new TenantId(1L)), any(KnowledgeDocumentBO.class));
        doAnswer(invocation -> { ((KnowledgeDocumentVersionBO) invocation.getArgument(1)).setId(32L); return null; })
                .when(enterprise).insertVersion(eq(new TenantId(1L)), any(KnowledgeDocumentVersionBO.class));
        EnterpriseDocumentServiceImpl service = new EnterpriseDocumentServiceImpl(documents, enterprise, spaces, audit,
                relations, ingestion, index, tx, metadata, storage);
        var request = new EnterpriseDocumentService.StoredFileRequest(5L, "Existing", "readme",
                "objects/existing", null, "text/plain", 4L, "sha-existing");
        assertEquals(30L, service.registerStoredFile(actor, request).jobId());
        verify(documents, never()).selectById(new TenantId(1L), null);
        assertThrows(ServiceException.class, () -> service.get(null, 31L));

        KnowledgeDocumentBO published = document(40L, 5L, 41L, "PUBLISHED", "READY");
        KnowledgeDocumentVersionBO source = version(41L, 40L, 1, "PUBLISHED", "READY");
        when(documents.selectById(new TenantId(1L), 40L)).thenReturn(published);
        when(enterprise.findVersion(new TenantId(1L), 41L)).thenReturn(source);
        when(enterprise.findVersions(new TenantId(1L), 40L)).thenReturn(List.of(source));
        assertEquals(1, service.versions(actor, 40L).size());
        assertEquals(40L, service.get(actor, 40L).id());
        when(enterprise.nextVersionNo(new TenantId(1L), 40L)).thenReturn(2);
        doAnswer(invocation -> { ((KnowledgeDocumentVersionBO) invocation.getArgument(1)).setId(42L); return null; })
                .when(enterprise).insertVersion(eq(new TenantId(1L)), any(KnowledgeDocumentVersionBO.class));
        TransactionSynchronizationManager.initSynchronization();
        try {
            assertEquals(42L, service.rollback(actor, 40L, 41L).currentVersionId());
            TransactionSynchronizationManager.getSynchronizations().forEach(sync -> sync.afterCommit());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
        published.setLifecycleStatus("PUBLISHED");
        when(enterprise.findVersion(new TenantId(1L), 42L)).thenReturn(version(42L, 40L, 2, "PUBLISHED", "READY"));
        TransactionSynchronizationManager.initSynchronization();
        try {
            assertEquals("ARCHIVED", service.archive(actor, 40L, "archive").lifecycleStatus());
            TransactionSynchronizationManager.getSynchronizations().forEach(sync -> sync.afterCommit());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        published.setLifecycleStatus("REVIEWING");
        published.setParseStatus("READY");
        when(enterprise.findVersion(new TenantId(1L), 42L)).thenReturn(version(42L, 40L, 2, "REVIEWING", "READY"));
        TransactionSynchronizationManager.initSynchronization();
        try {
            assertEquals("PUBLISHED", service.approve(actor, 40L, "approve").lifecycleStatus());
            TransactionSynchronizationManager.getSynchronizations().forEach(sync -> sync.afterCommit());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
        published.setLifecycleStatus("REVIEWING");
        assertEquals("DRAFT", service.reject(actor, 40L, "reject").lifecycleStatus());

        KnowledgeSpaceBO requiredBinding = new KnowledgeSpaceBO();
        requiredBinding.setBindingMode("REQUIRED");
        when(enterprise.findSpace(new TenantId(1L), 5L)).thenReturn(requiredBinding);
        when(relations.listPointIds(actor, 40L)).thenReturn(List.of());
        assertThrows(ServiceException.class, () -> service.publish(actor, 40L, "publish"));

        when(tx.execute(any(), any())).thenAnswer(invocation -> ((java.util.function.Supplier<?>) invocation.getArgument(1)).get());
        published.setObjectKey("objects/failing-delete");
        when(documents.selectById(new TenantId(1L), 40L)).thenReturn(published);
        doThrow(new IOException("storage unavailable")).when(storage).delete("objects/failing-delete");
        service.delete(actor, 40L);
        verify(metadata, never()).markObjectDeleted(anyLong(), anyString());
        doThrow(new IllegalStateException("index unavailable")).when(index).rebuild(new TenantId(1L), 5L);
        assertThrows(ServiceException.class, () -> service.delete(actor, 40L));
    }

    private static KnowledgeDocumentBO document(Long id, Long spaceId, Long versionId, String lifecycle, String parse) {
        KnowledgeDocumentBO value = new KnowledgeDocumentBO(); value.setId(id); value.setTenantId(1L); value.setSpaceId(spaceId); value.setCurrentVersionId(versionId); value.setTitle("Guide"); value.setLifecycleStatus(lifecycle); value.setParseStatus(parse); return value;
    }

    private static KnowledgeDocumentVersionBO version(Long id, Long documentId, int versionNo, String lifecycle, String parse) {
        KnowledgeDocumentVersionBO value = new KnowledgeDocumentVersionBO(); value.setId(id); value.setDocumentId(documentId); value.setSpaceId(5L); value.setVersionNo(versionNo); value.setTitle("Guide"); value.setLifecycleStatus(lifecycle); value.setParseStatus(parse); return value;
    }

    private ActorContext actor(long tenantId) {
        return new ActorContext(new TenantId(tenantId), new UserId(7L), new RoleId(3L), false);
    }
}
