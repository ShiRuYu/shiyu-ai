package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.common.storage.ContentSecurityScanner;
import com.shiyu.ai.common.storage.ObjectStorage;
import com.shiyu.ai.knowledge.service.EnterpriseDocumentService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
class KnowledgeDocumentUploadServiceImplTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(7L), new UserId(9L), false);

    @Test
    void uploadsSecureFilesWithFallbackTitleAndDeletesDuplicateObjects() throws Exception {
        ObjectStorage storage = mock(ObjectStorage.class);
        ContentSecurityScanner scanner = mock(ContentSecurityScanner.class);
        EnterpriseDocumentService documents = mock(EnterpriseDocumentService.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        ObjectStorage.StoredObject stored = new ObjectStorage.StoredObject("obj/1", "a.txt", "text/plain", 3, "local");
        EnterpriseDocumentService.UploadResult result = new EnterpriseDocumentService.UploadResult(null, 1L, 2L, false);
        when(storage.put(anyString(), eq("a.txt"), eq("text/plain"), eq(3L), any())).thenReturn(stored);
        when(documents.registerStoredFile(eq(ACTOR), any())).thenReturn(result);
        KnowledgeDocumentUploadServiceImpl service = new KnowledgeDocumentUploadServiceImpl(storage, scanner, documents, spaces);

        assertSame(result, service.upload(ACTOR, 10L, "  ", "a.txt", "text/plain", new byte[]{1, 2, 3}));
        verify(spaces).requireAccess(eq(10L), eq(KnowledgeSpaceService.SpaceRole.EDITOR), eq(ACTOR));
        verify(scanner).validate("a.txt", "text/plain", new byte[]{1, 2, 3});
        verify(documents).registerStoredFile(eq(ACTOR), argThat(request -> "a.txt".equals(request.title())
                && "obj/1".equals(request.objectKey()) && "a.txt".equals(request.originalName())));

        when(documents.registerStoredFile(eq(ACTOR), any())).thenReturn(new EnterpriseDocumentService.UploadResult(null, 1L, 2L, true));
        service.upload(ACTOR, 10L, "Title", "a.txt", "text/plain", new byte[]{1, 2, 3});
        verify(storage).delete("obj/1");
    }

    @Test
    void translatesStorageAndRegistrationFailuresAndRejectsMissingActor() throws Exception {
        ObjectStorage storage = mock(ObjectStorage.class);
        ContentSecurityScanner scanner = mock(ContentSecurityScanner.class);
        EnterpriseDocumentService documents = mock(EnterpriseDocumentService.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeDocumentUploadServiceImpl service = new KnowledgeDocumentUploadServiceImpl(storage, scanner, documents, spaces);
        assertThrows(ServiceException.class, () -> service.upload(null, 10L, "t", "a.txt", "text/plain", new byte[]{1}));

        when(storage.put(anyString(), anyString(), anyString(), anyLong(), any())).thenThrow(new IOException("disk"));
        assertThrows(ServiceException.class, () -> service.upload(ACTOR, 10L, "t", "a.txt", "text/plain", new byte[]{1}));

        ObjectStorage.StoredObject stored = new ObjectStorage.StoredObject("obj/2", "a.txt", "text/plain", 1, "local");
        doReturn(stored).when(storage).put(anyString(), anyString(), anyString(), anyLong(), any());
        when(documents.registerStoredFile(eq(ACTOR), any())).thenThrow(new IllegalStateException("db"));
        assertThrows(IllegalStateException.class, () -> service.upload(ACTOR, 10L, "t", "a.txt", "text/plain", new byte[]{1}));
        verify(storage).delete("obj/2");
        doThrow(new IOException("cleanup")).when(storage).delete("obj/2");
        assertThrows(IllegalStateException.class, () -> service.upload(ACTOR, 10L, "t", "a.txt", "text/plain", new byte[]{1}));
    }

    @Test
    void validatesImportedUrlBeforeNetworkAccess() {
        ObjectStorage storage = mock(ObjectStorage.class);
        ContentSecurityScanner scanner = mock(ContentSecurityScanner.class);
        EnterpriseDocumentService documents = mock(EnterpriseDocumentService.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeDocumentUploadServiceImpl service = new KnowledgeDocumentUploadServiceImpl(storage, scanner, documents, spaces);
        assertThrows(ServiceException.class, () -> service.importUrl(ACTOR, 10L, "t", "not a url"));
        assertThrows(ServiceException.class, () -> service.importUrl(ACTOR, 10L, "t", "ftp://example.com/a"));
        assertThrows(ServiceException.class, () -> service.importUrl(ACTOR, 10L, "t", "http://127.0.0.1/a"));
        assertThrows(ServiceException.class, () -> service.importUrl(ACTOR, 10L, "t", null));
    }

    @Test
    void coversImportTransportFailuresAndPrivateUrlHelpers() throws Exception {
        ObjectStorage storage = mock(ObjectStorage.class);
        ContentSecurityScanner scanner = mock(ContentSecurityScanner.class);
        EnterpriseDocumentService documents = mock(EnterpriseDocumentService.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeDocumentUploadServiceImpl service = new KnowledgeDocumentUploadServiceImpl(storage, scanner, documents, spaces);
        HttpClient client = mock(HttpClient.class);
        Field clientField = KnowledgeDocumentUploadServiceImpl.class.getDeclaredField("httpClient");
        clientField.setAccessible(true);
        clientField.set(service, client);

        HttpResponse<byte[]> response = mock(HttpResponse.class);
        HttpHeaders headers = HttpHeaders.of(Map.of("Content-Type", List.of("text/plain; charset=utf-8")),
                (name, value) -> true);
        when(response.headers()).thenReturn(headers);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("hello".getBytes());
        ObjectStorage.StoredObject stored = new ObjectStorage.StoredObject("obj/url", "path.txt", "text/plain", 5, "local");
        when(storage.put(anyString(), eq("path.txt"), eq("text/plain"), eq(5L), any())).thenReturn(stored);
        when(documents.registerStoredFile(eq(ACTOR), any()))
                .thenReturn(new EnterpriseDocumentService.UploadResult(null, 1L, 2L, false));
        doReturn(response).when(client).send(any(), any());
        assertNotNull(service.importUrl(ACTOR, 10L, " ", "http://198.51.100.1/path.txt"));
        verify(scanner).validate(eq("path.txt"), eq("text/plain"), any());

        when(response.statusCode()).thenReturn(503);
        assertThrows(ServiceException.class, () -> service.importUrl(ACTOR, 10L, "t", "http://198.51.100.1/error"));
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(new byte[0]);
        assertThrows(ServiceException.class, () -> service.importUrl(ACTOR, 10L, "t", "http://198.51.100.1/empty"));
        doThrow(new IOException("network")).when(client).send(any(), any());
        assertThrows(ServiceException.class, () -> service.importUrl(ACTOR, 10L, "t", "http://198.51.100.1/io"));
        doThrow(new InterruptedException("cancelled")).when(client).send(any(), any());
        assertThrows(ServiceException.class, () -> service.importUrl(ACTOR, 10L, "t", "http://198.51.100.1/interrupted"));
        assertTrue(Thread.interrupted());

        assertEquals("web-page.html", invoke(service, "fileName", URI.create("http://example.test")));
        assertEquals("web-page.html", invoke(service, "fileName", URI.create("http://example.test/")));
        assertEquals("web-page.html", invoke(service, "fileName", URI.create("http://example.test")));
        assertEquals("doc.pdf", invoke(service, "fileName", URI.create("http://example.test/a/doc.pdf")));
        assertThrows(ServiceException.class, () -> invoke(service, "validateExternalUrl", URI.create("ftp://example.test/a")));
        assertThrows(ServiceException.class, () -> invoke(service, "validateExternalUrl", URI.create("http://127.0.0.1/a")));
        assertDoesNotThrow(() -> invoke(service, "validateExternalUrl", URI.create("http://198.51.100.1/a")));
        assertEquals(64, ((String) invoke(service, "sha256", "hello".getBytes())).length());
        try (var mockedDigest = mockStatic(MessageDigest.class)) {
            mockedDigest.when(() -> MessageDigest.getInstance("SHA-256"))
                    .thenThrow(new NoSuchAlgorithmException("missing"));
            assertThrows(IllegalStateException.class, () -> invoke(service, "sha256", "hello".getBytes()));
        }
    }

    private static Object invoke(KnowledgeDocumentUploadServiceImpl service, String name, Object... args)
            throws Exception {
        for (var method : KnowledgeDocumentUploadServiceImpl.class.getDeclaredMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != args.length) continue;
            method.setAccessible(true);
            try {
                return method.invoke(service, args);
            } catch (java.lang.reflect.InvocationTargetException exception) {
                Throwable cause = exception.getCause();
                if (cause instanceof Exception checked) throw checked;
                if (cause instanceof Error error) throw error;
                throw exception;
            }
        }
        throw new NoSuchMethodException(name);
    }
}
