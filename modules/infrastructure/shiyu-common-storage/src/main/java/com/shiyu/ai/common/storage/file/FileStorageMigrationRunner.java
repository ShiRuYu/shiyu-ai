package com.shiyu.ai.common.storage.file;

import com.shiyu.ai.common.storage.api.StorageMetadataStore;
import com.shiyu.ai.common.storage.api.StoredFile;
import com.shiyu.ai.common.storage.config.StorageMigrationProperties;
import com.shiyu.ai.common.storage.config.StorageProperties;
import com.shiyu.ai.common.storage.metadata.NoopStorageMetadataStore;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * One-shot local to S3-compatible migration. It is disabled by default and keeps every source key
 * unchanged; metadata remains owned by the application database.
 */
@Component
@Slf4j
public class FileStorageMigrationRunner implements ApplicationRunner {

    private final StorageMigrationProperties migration;
    private final StorageProperties storageProperties;
    private final StorageMetadataStore metadataStore;

    public FileStorageMigrationRunner(
            StorageMigrationProperties migration,
            StorageProperties storageProperties,
            ObjectProvider<StorageMetadataStore> metadataStores) {
        this.migration = migration;
        this.storageProperties = storageProperties;
        this.metadataStore = metadataStores.getIfAvailable(() -> NoopStorageMetadataStore.INSTANCE);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        migration.validate();
        if (!migration.isEnabled()) return;

        String destinationType = migration.getDestinationProvider().trim().toLowerCase(Locale.ROOT);
        StorageProperties destination = copyForDestination(destinationType);
        try (FileStorageManager target = new FileStorageManager(destination)) {
            LocalFileStorage source =
                    new LocalFileStorage(Path.of(migration.getSourcePath()), "local");
            List<StoredFile> objects = source.list(migration.getNamespace());
            int copied = 0;
            for (StoredFile object : objects) {
                try (var input = source.open(object.key()).inputStream()) {
                    target.uploadAtKey(
                            object.key(),
                            object.name(),
                            object.contentType(),
                            object.size(),
                            input);
                    updateMetadataProvider(object.key(), destinationType);
                    copied++;
                }
            }
            log.info(
                    "File storage migration completed: provider={}, namespace={}, copied={}",
                    destinationType,
                    migration.getNamespace(),
                    copied);
        }
    }

    private void updateMetadataProvider(String key, String provider) throws IOException {
        long tenantId = tenantId(key);
        metadataStore.updateObjectProvider(tenantId, key, provider);
    }

    private long tenantId(String key) throws IOException {
        if (key == null) throw new IOException("文件标识不能为空");
        for (String segment : key.split("/")) {
            try {
                return Long.parseLong(segment);
            } catch (NumberFormatException ignored) {
                // Namespace labels are opaque; the first numeric segment is the tenant id.
            }
        }
        throw new IOException("文件标识缺少租户标识: " + key);
    }

    private StorageProperties copyForDestination(String destinationType) {
        StorageProperties destination = new StorageProperties();
        destination.setType(destinationType);
        destination.setLocal(storageProperties.getLocal());
        destination.setProviders(storageProperties.getProviders());
        return destination;
    }
}
