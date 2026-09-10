package com.shiyu.ai.common.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Provider override for file storage while retaining the legacy storage.type key. */
@ConfigurationProperties(prefix = "shiyu.infrastructure.file")
public class FileInfrastructureProperties {

    private String provider;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String resolveProvider(String legacyType) {
        return provider == null || provider.isBlank() ? legacyType : provider.trim();
    }
}
