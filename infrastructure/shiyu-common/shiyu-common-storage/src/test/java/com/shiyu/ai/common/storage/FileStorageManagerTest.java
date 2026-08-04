package com.shiyu.ai.common.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("dev")
@Tag("prod")
class FileStorageManagerTest {

    @TempDir
    Path tempDirectory;

    @Test
    void shouldUseLocalStorageByDefault() throws Exception {
        StorageProperties properties = new StorageProperties();
        properties.getLocal().setPath(tempDirectory.toString());

        try (FileStorageManager manager = new FileStorageManager(properties)) {
            assertEquals("local", manager.type());
            assertTrue(manager.list("tenant/1").isEmpty());
        }
    }

    @Test
    void shouldRejectUnsupportedStorageType() {
        StorageProperties properties = new StorageProperties();
        properties.setType("ftp");

        assertThrows(IllegalStateException.class, () -> new FileStorageManager(properties));
    }

    @Test
    void shouldRequireSelectedExternalProviderConfiguration() {
        StorageProperties properties = new StorageProperties();
        properties.setType("minio");

        assertThrows(IllegalStateException.class, () -> new FileStorageManager(properties));
    }

    @Test
    void shouldCreateAllSupportedS3CompatibleProviders() throws Exception {
        for (String type : new String[]{"s3", "minio", "aliyun-oss", "tencent-cos"}) {
            StorageProperties properties = new StorageProperties();
            properties.setType(type);
            StorageProperties.S3Provider provider = new StorageProperties.S3Provider();
            provider.setEndpoint("http://127.0.0.1:9001");
            provider.setBucket("test-bucket");
            provider.setAccessKey("test-access-key");
            provider.setSecretKey("test-secret-key");
            properties.getProviders().put(type, provider);

            try (FileStorageManager manager = new FileStorageManager(properties)) {
                assertEquals(type, manager.type());
            }
        }
    }
}
