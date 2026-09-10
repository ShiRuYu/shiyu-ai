package com.shiyu.ai.common.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Explicit, one-shot local object migration settings. */
@ConfigurationProperties(prefix = "shiyu.storage.migration")
public class StorageMigrationProperties {

    private boolean enabled;
    private String sourcePath;
    private String destinationProvider;
    private String namespace;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getSourcePath() { return sourcePath; }
    public void setSourcePath(String sourcePath) { this.sourcePath = sourcePath; }
    public String getDestinationProvider() { return destinationProvider; }
    public void setDestinationProvider(String destinationProvider) { this.destinationProvider = destinationProvider; }
    public String getNamespace() { return namespace; }
    public void setNamespace(String namespace) { this.namespace = namespace; }

    public void validate() {
        if (!enabled) return;
        if (sourcePath == null || sourcePath.isBlank()) {
            throw new IllegalStateException("启用文件迁移时必须配置 shiyu.storage.migration.source-path");
        }
        if (destinationProvider == null || destinationProvider.isBlank()
                || !("s3".equalsIgnoreCase(destinationProvider)
                || "minio".equalsIgnoreCase(destinationProvider))) {
            throw new IllegalStateException("文件迁移目标必须是 s3 或 minio");
        }
        if (namespace == null || namespace.isBlank()) {
            throw new IllegalStateException("启用文件迁移时必须配置 shiyu.storage.migration.namespace");
        }
    }
}
