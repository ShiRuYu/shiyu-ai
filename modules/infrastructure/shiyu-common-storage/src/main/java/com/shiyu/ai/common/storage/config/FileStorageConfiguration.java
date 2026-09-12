package com.shiyu.ai.common.storage.config;
import com.shiyu.ai.common.storage.file.service.FileStorageManager;

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

/**
 * {@code FileStorageConfiguration} 提供平台基础设施模块的配置项，并集中声明其默认值和运行约束。
 */
@Configuration
@EnableConfigurationProperties({
    StorageProperties.class,
    FileInfrastructureProperties.class,
    StorageMigrationProperties.class
})
public class FileStorageConfiguration {

    /**
     * {@code fileStorageManager} 执行当前类型定义的业务操作。
     *
     * @param properties 参数值，用于执行当前操作。
     * @param infrastructureProperties 参数值，用于执行当前操作。
     * @param metadataStores 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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
