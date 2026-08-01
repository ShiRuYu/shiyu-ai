package com.shiyu.ai.common.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;

import java.io.IOException;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class FileStorageConfiguration {

    @Bean(destroyMethod = "close")
    public FileStorageManager fileStorageManager(StorageProperties properties,
                                                 ObjectProvider<StorageMetadataStore> metadataStores) throws IOException {
        return new FileStorageManager(properties,
                metadataStores.getIfAvailable(() -> NoopStorageMetadataStore.INSTANCE));
    }
}
