package com.shiyu.ai.common.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * StorageMigrationProperties 配置属性，集中管理基础设施领域相关运行参数。
 */
@ConfigurationProperties(prefix = "shiyu.storage.migration")
@Getter
@Setter
public class StorageMigrationProperties {

    /**
     * 启用开关，表示当前对象中的对应属性。
     */
    private boolean enabled;
    /**
     * 来源路径，表示当前对象中的对应属性。
     */
    private String sourcePath;
    /**
     * destinationProvider 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String destinationProvider;
    /**
     * namespace 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String namespace;

    /**
     * {@code validate} 校验当前操作的输入或状态是否满足约束。
     */
    public void validate() {
        if (!enabled) return;
        if (sourcePath == null || sourcePath.isBlank()) {
            throw new IllegalStateException("启用文件迁移时必须配置 shiyu.storage.migration.source-path");
        }
        if (destinationProvider == null
                || destinationProvider.isBlank()
                || !("s3".equalsIgnoreCase(destinationProvider)
                        || "minio".equalsIgnoreCase(destinationProvider))) {
            throw new IllegalStateException("文件迁移目标必须是 s3 或 minio");
        }
        if (namespace == null || namespace.isBlank()) {
            throw new IllegalStateException("启用文件迁移时必须配置 shiyu.storage.migration.namespace");
        }
    }
}
