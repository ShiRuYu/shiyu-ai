package com.shiyu.ai.common.storage.file;
import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;

class ResumableUploadServiceTest {

    @TempDir
    Path tempDirectory;

    @Test
    void rejectsMissingTenantBeforeStartingUpload() {
        StorageProperties properties = new StorageProperties();
        properties.setType("local");
        properties.getLocal().setPath(tempDirectory.resolve("uploads").toString());
        ResumableUploadService service = new ResumableUploadService(properties,
                mock(ObjectStorage.class), mock(ContentSecurityScanner.class),
                mock(ResumableUploadHandler.class), mock(StorageMetadataStore.class));

        assertThrows(RuntimeException.class, () -> service.begin(
                null, 1L, new ResumableUploadService.BeginRequest(
                        "example.txt", "text/plain", 1L, null, null)));
    }

    @Test
    void hidesLocalStorageDetailsWhenCreatingUploadSessionFails() throws Exception {
        StorageProperties properties = new StorageProperties();
        properties.setType("local");
        Path configuredFile = tempDirectory.resolve("storage-secret-path");
        Files.writeString(configuredFile, "not a directory");
        properties.getLocal().setPath(configuredFile.toString());
        ResumableUploadHandler handler = mock(ResumableUploadHandler.class);
        ResumableUploadService service = new ResumableUploadService(properties,
                mock(ObjectStorage.class), mock(ContentSecurityScanner.class), handler,
                mock(StorageMetadataStore.class));
        ResumableUploadHandler.UploadActor actor = new ResumableUploadHandler.UploadActor(
                new TenantId(1L), new UserId(2L), null, false);

        ServiceException failure = assertThrows(ServiceException.class, () -> service.begin(
                actor, 1L, new ResumableUploadService.BeginRequest(
                        "example.txt", "text/plain", 1L, null, null)));

        assertEquals("创建上传会话失败", failure.getMessage());
    }

    @Test
    void rejectsUploadActorWithoutAUser() {
        assertThrows(IllegalArgumentException.class,
                () -> new ResumableUploadHandler.UploadActor(new TenantId(1L), null, null, false));
    }

    @Test
    void removesExpiredPersistentSessionsAndLocalOrphans() throws Exception {
        StorageProperties properties = new StorageProperties();
        properties.setType("local");
        properties.getLocal().setPath(tempDirectory.resolve("uploads").toString());
        Path chunkRoot = tempDirectory.resolve("uploads/.chunks");

        Path persistedSession = chunkRoot.resolve("persisted-session");
        Files.createDirectories(persistedSession);
        Files.writeString(persistedSession.resolve("part-0"), "expired");

        Path orphanSession = chunkRoot.resolve("orphan-session");
        Files.createDirectories(orphanSession);
        Properties metadata = new Properties();
        metadata.setProperty("expiresAt", Instant.now().minusSeconds(60).toString());
        try (var output = Files.newOutputStream(orphanSession.resolve("metadata.properties"))) {
            metadata.store(output, "expired upload");
        }

        StorageMetadataStore metadataStore = mock(StorageMetadataStore.class);
        StorageMetadataStore.UploadSessionRecord expired = new StorageMetadataStore.UploadSessionRecord(
                "persisted-session", 1L, 1L, "tenant/1/knowledge/1", "expired.txt", "text/plain",
                7L, null, 1, "UPLOADING", persistedSession.toString(), Instant.now().minusSeconds(60));
        when(metadataStore.findExpiredUploadSessions(any())).thenReturn(List.of(expired));

        ResumableUploadService service = new ResumableUploadService(properties,
                mock(ObjectStorage.class), mock(ContentSecurityScanner.class),
                mock(ResumableUploadHandler.class), metadataStore);

        service.cleanupExpiredSessions();

        assertFalse(Files.exists(persistedSession));
        assertFalse(Files.exists(orphanSession));
        verify(metadataStore).deleteUploadSession("persisted-session");
    }

    @Test
    void storesGenericFailureWhenRegistrationFailsAfterObjectUpload() throws Exception {
        StorageProperties properties = new StorageProperties();
        properties.setType("local");
        properties.getLocal().setPath(tempDirectory.resolve("uploads").toString());
        String id = "1234567890123456";
        Path directory = tempDirectory.resolve("uploads/.chunks").resolve(id);
        Files.createDirectories(directory);
        Properties metadata = new Properties();
        metadata.setProperty("tenantId", "1");
        metadata.setProperty("spaceId", "7");
        metadata.setProperty("namespace", "tenant/1/knowledge/7");
        metadata.setProperty("fileName", "example.txt");
        metadata.setProperty("contentType", "text/plain");
        metadata.setProperty("size", "4");
        metadata.setProperty("totalChunks", "1");
        metadata.setProperty("checksum", "");
        metadata.setProperty("title", "");
        try (var output = Files.newOutputStream(directory.resolve("metadata.properties"))) {
            metadata.store(output, "test");
        }
        Files.writeString(directory.resolve("part-0"), "data");

        ObjectStorage storage = mock(ObjectStorage.class);
        ContentSecurityScanner scanner = mock(ContentSecurityScanner.class);
        ResumableUploadHandler uploadHandler = mock(ResumableUploadHandler.class);
        StorageMetadataStore metadataStore = mock(StorageMetadataStore.class);
        when(metadataStore.persistent()).thenReturn(true);
        when(storage.put(any(), any(), any(), any(Long.class), any())).thenReturn(
                new ObjectStorage.StoredObject("object-key", "example.txt", "text/plain", 4, "local"));
        doThrow(new IllegalStateException("database password=secret"))
                .when(uploadHandler).register(any(), any());

        ResumableUploadService service = new ResumableUploadService(properties, storage, scanner,
                uploadHandler, metadataStore);
        ResumableUploadHandler.UploadActor actor = new ResumableUploadHandler.UploadActor(
                new TenantId(1L), new UserId(2L), null, false);

        assertThrows(IllegalStateException.class, () -> service.complete(actor, id));
        verify(metadataStore).updateUploadSessionStatus(eq(id), eq("FAILED"), eq("上传文件失败"));
    }
}
