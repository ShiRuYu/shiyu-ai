package com.shiyu.ai.common.storage.config;
import com.shiyu.ai.common.storage.file.service.FileStorageManager;

import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
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
 * 定义 文件 Storage 基础设施或应用能力的配置项及装配规则。
 */
@Configuration
@EnableConfigurationProperties({
    StorageProperties.class,
    FileInfrastructureProperties.class,
    StorageMigrationProperties.class
})
public class FileStorageConfiguration {

    /**
     * 执行 文件 Storage 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param close 用于完成本次业务处理的 close 参数。
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
