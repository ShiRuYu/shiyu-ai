package com.shiyu.ai.core.langchain4j.config;

import lombok.Data;

@Data
public class PlatformConfig {

    private String platformType;
    private String baseUrl;
    private String apiKey;
    private String modelName;
    private Double temperature = 0.7;
    private Integer maxTokens = 4096;
    private Integer maxRetries = 3;

    public PlatformConfig() {
    }

    public PlatformConfig(String platformType, String baseUrl, String apiKey,
                          String modelName, Double temperature,
                          Integer maxTokens, Integer maxRetries) {
        this.platformType = platformType;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.maxRetries = maxRetries;
    }

    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    public boolean isBaseUrlConfigured() {
        return baseUrl != null && !baseUrl.trim().isEmpty();
    }
}
