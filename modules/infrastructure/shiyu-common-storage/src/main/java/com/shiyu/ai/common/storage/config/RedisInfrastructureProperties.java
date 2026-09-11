package com.shiyu.ai.common.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Locale;

/** Configuration for the optional Redis-backed coordination stores. */
@ConfigurationProperties(prefix = "shiyu.infrastructure.redis")
public class RedisInfrastructureProperties {

    private String provider = "disabled";
    private String url = "redis://127.0.0.1:6379/0";
    private String password;
    private String keyPrefix = "shiyu";

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String normalizedProvider() {
        return provider == null || provider.isBlank()
                ? "disabled"
                : provider.trim().toLowerCase(Locale.ROOT);
    }

    public void validate() {
        String selected = normalizedProvider();
        if (!"disabled".equals(selected) && !"redis".equals(selected)) {
            throw new IllegalStateException(
                    "Unsupported Redis provider: " + selected + "; expected disabled or redis");
        }
        if ("redis".equals(selected) && (url == null || url.isBlank())) {
            throw new IllegalStateException(
                    "Redis provider requires shiyu.infrastructure.redis.url");
        }
    }

    public String key(String... parts) {
        String prefix = keyPrefix == null || keyPrefix.isBlank() ? "shiyu" : keyPrefix.trim();
        StringBuilder key = new StringBuilder(prefix);
        for (String part : parts) {
            if (part != null && !part.isBlank()) key.append(':').append(part);
        }
        return key.toString();
    }
}
