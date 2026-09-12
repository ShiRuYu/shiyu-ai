package com.shiyu.ai.common.storage.file.migration;
import com.shiyu.ai.common.storage.file.adapter.LocalFileStorage;
import com.shiyu.ai.common.storage.file.service.FileStorageManager;

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
 * 在应用启动时迁移和校验文件存储目录结构。
 */
@Component
@Slf4j
public class FileStorageMigrationRunner implements ApplicationRunner {

    /**
     * migration 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final StorageMigrationProperties migration;
    /**
     * storageProperties 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final StorageProperties storageProperties;
    /**
     * 元数据存储，表示当前对象中的对应属性。
     */
    private final StorageMetadataStore metadataStore;

    /**
     * {@code FileStorageMigrationRunner} 创建并初始化当前类型实例。
     *
     * @param migration 参数值，用于执行当前操作。
     * @param storageProperties 参数值，用于执行当前操作。
     * @param metadataStores 参数值，用于执行当前操作。
     */
    public FileStorageMigrationRunner(
            StorageMigrationProperties migration,
            StorageProperties storageProperties,
            ObjectProvider<StorageMetadataStore> metadataStores) {
        this.migration = migration;
        this.storageProperties = storageProperties;
        this.metadataStore = metadataStores.getIfAvailable(() -> NoopStorageMetadataStore.INSTANCE);
    }

    /**
     * {@code run} 执行当前模块定义的业务流程。
     *
     * @param args 参数值，用于执行当前操作。
     */
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
