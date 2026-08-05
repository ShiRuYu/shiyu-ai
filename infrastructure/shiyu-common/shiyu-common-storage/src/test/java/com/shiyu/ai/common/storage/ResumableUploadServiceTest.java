package com.shiyu.ai.common.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResumableUploadServiceTest {

    @TempDir
    Path tempDirectory;

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
}
