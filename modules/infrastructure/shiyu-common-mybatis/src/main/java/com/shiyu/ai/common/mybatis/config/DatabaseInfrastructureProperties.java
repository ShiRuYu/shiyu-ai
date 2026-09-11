package com.shiyu.ai.common.mybatis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Locale;
import java.util.Set;

/** Selects the database profile without changing repository code. */
@ConfigurationProperties(prefix = "shiyu.infrastructure.database")
public class DatabaseInfrastructureProperties {

    private static final Set<String> SUPPORTED = Set.of("h2", "mysql", "postgresql");

    private String provider = "h2";

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String normalizedProvider() {
        return provider == null || provider.isBlank()
                ? "h2"
                : provider.trim().toLowerCase(Locale.ROOT);
    }

    public void validate() {
        String normalized = normalizedProvider();
        if (!SUPPORTED.contains(normalized)) {
            throw new IllegalArgumentException(
                    "不支持的数据库 provider: " + provider + "，可用类型: " + SUPPORTED);
        }
    }
}
