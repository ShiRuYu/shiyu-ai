package com.shiyu.ai.common.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("dev")
@Tag("prod")
class LocalFileStorageTest {

    @TempDir
    Path tempDirectory;

    @Test
    void shouldUploadListDownloadAndDeleteWithinTenantNamespace() throws Exception {
        LocalFileStorage storage = new LocalFileStorage(tempDirectory, "local");
        byte[] content = "文件存储测试".getBytes(StandardCharsets.UTF_8);

        StoredFile uploaded = storage.upload(
                "tenant/1/",
                "测试资料.txt",
                "text/plain",
                content.length,
                new ByteArrayInputStream(content));

        assertTrue(uploaded.key().startsWith("tenant/1/"));
        assertEquals("测试资料.txt", uploaded.name());
        assertEquals(1, storage.list("tenant/1/").size());
        assertTrue(storage.list("tenant/2/").isEmpty());

        try (var inputStream = storage.open(uploaded.key()).inputStream()) {
            assertArrayEquals(content, inputStream.readAllBytes());
        }

        storage.delete(uploaded.key());
        assertTrue(storage.list("tenant/1/").isEmpty());
    }

    @Test
    void shouldRejectPathTraversal() throws Exception {
        LocalFileStorage storage = new LocalFileStorage(tempDirectory, "local");

        assertThrows(Exception.class, () -> storage.open("../secret.txt"));
        assertThrows(Exception.class, () -> storage.delete("tenant/1/../../secret.txt"));
    }

    @Test
    void shouldExposeMissingObjectsAsFileNotFoundExceptions() throws Exception {
        LocalFileStorage storage = new LocalFileStorage(tempDirectory, "local");

        assertThrows(FileNotFoundException.class, () -> storage.open("tenant/1/missing.txt"));
        assertThrows(FileNotFoundException.class, () -> storage.delete("tenant/1/missing.txt"));
    }
}
