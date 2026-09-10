package com.shiyu.ai.common.vector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Provider override for vector storage while retaining vector-store.type. */
@ConfigurationProperties(prefix = "shiyu.infrastructure.vector")
public class VectorInfrastructureProperties {

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
