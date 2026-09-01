package com.shiyu.ai.model.adapter.config;

import lombok.Data;

@Data
public class PlatformConfig {

    private String platformType;
    private String adapterType = "OPENAI_COMPATIBLE";
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
        this(platformType,
                "OLLAMA".equalsIgnoreCase(platformType) ? "OLLAMA" : "OPENAI_COMPATIBLE",
                baseUrl, apiKey, modelName, temperature, maxTokens, maxRetries);
    }

    public PlatformConfig(String platformType, String adapterType, String baseUrl, String apiKey,
                          String modelName, Double temperature,
                          Integer maxTokens, Integer maxRetries) {
        this.platformType = platformType;
        this.adapterType = adapterType;
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
