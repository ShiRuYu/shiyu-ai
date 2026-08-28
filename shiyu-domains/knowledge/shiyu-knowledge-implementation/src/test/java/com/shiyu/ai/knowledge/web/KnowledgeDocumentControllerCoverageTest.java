package com.shiyu.ai.knowledge.web;

import com.shiyu.ai.common.core.domain.UserContext;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.common.storage.ObjectStorage;
import com.shiyu.ai.common.storage.ResumableUploadHandler;
import com.shiyu.ai.common.storage.ResumableUploadService;
import com.shiyu.ai.knowledge.service.EnterpriseDocumentService;
import com.shiyu.ai.knowledge.service.KnowledgeDocumentUploadService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class KnowledgeDocumentControllerCoverageTest {
    private final EnterpriseDocumentService documents = mock(EnterpriseDocumentService.class);
    private final KnowledgeDocumentUploadService uploads = mock(KnowledgeDocumentUploadService.class);
    private final ObjectStorage storage = mock(ObjectStorage.class);
    private final ResumableUploadService resumable = mock(ResumableUploadService.class);
    private final KnowledgeDocumentController controller = new KnowledgeDocumentController(documents, uploads, storage, resumable);
    private final EnterpriseDocumentService.DocumentView document = new EnterpriseDocumentService.DocumentView(
            11L, 3L, 21L, "Guide", "pdf", "upload", "PUBLISHED", "READY", "docs/11", "application/pdf", 4L, "sum", LocalDateTime.now(), LocalDateTime.now());
    private final EnterpriseDocumentService.UploadResult uploadResult = new EnterpriseDocumentService.UploadResult(document, 21L, 31L, false);

    @BeforeEach
    void actor() {
        UserContext context = new UserContext();
        context.setUserId(8L); context.setCurrentTenantId(7L); context.setHomeTenantId(7L);
        UserContextHolder.setContext(context);
    }

    @AfterEach
    void clear() { UserContextHolder.clearContext(); }

    @Test
    void mapsDocumentLifecycleAndUploadEndpoints() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "guide.pdf", "application/pdf", "data".getBytes());
        when(documents.page(any(), eq(3L), eq(1), eq(100), any(), any(), any())).thenReturn(null);
        when(uploads.upload(any(), eq(3L), eq("Guide"), eq("guide.pdf"), eq("application/pdf"), any())).thenReturn(uploadResult);
        when(uploads.importUrl(any(), eq(3L), eq("Guide"), eq("https://example.test/guide.pdf"))).thenReturn(uploadResult);
        when(documents.get(any(), eq(11L))).thenReturn(document);
        when(documents.versions(any(), eq(11L))).thenReturn(List.of());
        when(documents.submit(any(), eq(11L), eq("submit"))).thenReturn(document);
        when(documents.approve(any(), eq(11L), eq("approve"))).thenReturn(document);
        when(documents.reject(any(), eq(11L), eq("reject"))).thenReturn(document);
        when(documents.publish(any(), eq(11L), eq("publish"))).thenReturn(document);
        when(documents.archive(any(), eq(11L), eq("archive"))).thenReturn(document);
        when(documents.rollback(any(), eq(11L), eq(20L))).thenReturn(document);
        assertTrue(controller.page(3L, 1, 200, "q", "PUBLISHED", "READY", "v1").isSuccess());
        assertTrue(controller.upload(3L, file, "Guide", "1").isSuccess());
        assertTrue(controller.importUrl(3L, new KnowledgeDocumentController.ImportUrlRequest("https://example.test/guide.pdf", "Guide"), "1").isSuccess());
        assertTrue(controller.get(11L, "1").isSuccess());
        assertTrue(controller.versions(11L, "1").isSuccess());
        assertTrue(controller.submit(11L, "submit", "1").isSuccess());
        assertTrue(controller.approve(11L, "approve", "1").isSuccess());
        assertTrue(controller.reject(11L, "reject", "1").isSuccess());
        assertTrue(controller.publish(11L, "publish", "1").isSuccess());
        assertTrue(controller.archive(11L, "archive", "1").isSuccess());
        assertTrue(controller.rollback(11L, 20L, "1").isSuccess());
        assertTrue(controller.delete(11L, "1").isSuccess());
        verify(documents).delete(any(), eq(11L));
    }

    @Test
    void mapsResumableUploadEndpointsAndPreview() throws Exception {
        ResumableUploadService.BeginRequest begin = new ResumableUploadService.BeginRequest("guide.pdf", "application/pdf", 4L, null, "Guide");
        ResumableUploadService.UploadSession session = new ResumableUploadService.UploadSession("session-1", 3L, "guide.pdf", 4L, 1, List.of(), ResumableUploadService.CHUNK_SIZE);
        when(resumable.begin(any(), eq(3L), same(begin))).thenReturn(session);
        when(resumable.status(any(), eq("session-1"))).thenReturn(session);
        when(resumable.writeChunk(any(), eq("session-1"), eq(0), eq(1), any())).thenReturn(session);
        when(resumable.complete(any(), eq("session-1"))).thenReturn(new ResumableUploadHandler.RegistrationResult(uploadResult, false));
        when(documents.get(any(), eq(11L))).thenReturn(document);
        assertTrue(controller.beginUpload(3L, begin, "1").isSuccess());
        assertTrue(controller.uploadStatus("session-1", "1").isSuccess());
        MockMultipartFile chunk = new MockMultipartFile("file", "chunk", "application/octet-stream", "data".getBytes());
        assertTrue(controller.uploadChunk("session-1", 0, 1, chunk, "1").isSuccess());
        assertTrue(controller.completeUpload("session-1", "1").isSuccess());
        assertTrue(controller.cancelUpload("session-1", "1").isSuccess());

        ObjectStorage.ReadableObject readable = new ObjectStorage.ReadableObject(
                new ByteArrayInputStream("pdf".getBytes()), "中文 guide.pdf", null, 3L);
        when(storage.open("docs/11")).thenReturn(readable);
        assertEquals(200, controller.preview(11L, "1").getStatusCode().value());
        verify(storage).open("docs/11");
    }

    @Test
    void rejectsInvalidVersionAndEmptyOrUnreadableUploads() throws Exception {
        assertThrows(RuntimeException.class, () -> controller.page(3L, 1, 20, null, null, null, "2"));
        MockMultipartFile empty = new MockMultipartFile("file", "", "text/plain", new byte[0]);
        assertThrows(RuntimeException.class, () -> controller.upload(3L, empty, null, "1"));
        MockMultipartFile broken = mock(MockMultipartFile.class);
        when(broken.isEmpty()).thenReturn(false);
        when(broken.getOriginalFilename()).thenReturn("broken.txt");
        when(broken.getContentType()).thenReturn("text/plain");
        when(broken.getBytes()).thenThrow(new java.io.IOException("read failed"));
        assertThrows(RuntimeException.class, () -> controller.upload(3L, broken, null, "1"));
        when(documents.get(any(), eq(11L))).thenReturn(document);
        when(storage.open("docs/11")).thenThrow(new java.io.IOException("storage down"));
        assertThrows(RuntimeException.class, () -> controller.preview(11L, "1"));
    }
}
