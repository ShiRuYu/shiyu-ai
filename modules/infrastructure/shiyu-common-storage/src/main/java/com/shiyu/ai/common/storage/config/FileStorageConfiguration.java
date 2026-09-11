package com.shiyu.ai.common.storage.config;

import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@EnableConfigurationProperties({
    StorageProperties.class,
    FileInfrastructureProperties.class,
    StorageMigrationProperties.class
})
public class FileStorageConfiguration {

    @Bean(destroyMethod = "close")
    public FileStorageManager fileStorageManager(
            StorageProperties properties,
            FileInfrastructureProperties infrastructureProperties,
            ObjectProvider<StorageMetadataStore> metadataStores)
            throws IOException {
        properties.setType(infrastructureProperties.resolveProvider(properties.getType()));
        return new FileStorageManager(
                properties, metadataStores.getIfAvailable(() -> NoopStorageMetadataStore.INSTANCE));
    }
}
