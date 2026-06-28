package com.shiyu.ai.core.langchain4j.config;

import lombok.Data;

/**
 * LangChain4j 平台配置
 * 用于动态配置平台参数，支持运行时动态创建模型
 */
@Data
public class Lc4jPlatformConfig {
    
    /**
     * 平台类型（如：OPENROUTER, OLLAMA, DEEPSEEK, OPENAI, SILICON_FLOW）
     */
    private String platformType;
    
    /**
     * Base URL
     */
    private String baseUrl;
    
    /**
     * API Key
     */
    private String apiKey;
    
    /**
     * 模型名称
     */
    private String modelName;
    
    /**
     * 温度参数（默认 0.7）
     */
    private Double temperature = 0.7;
    
    /**
     * 最大 Token 数（默认 4096）
     */
    private Integer maxTokens = 4096;
    
    /**
     * 最大重试次数（默认 3）
     */
    private Integer maxRetries = 3;
    
    /**
     * 默认构造函数
     */
    public Lc4jPlatformConfig() {
    }
    
    /**
     * 全参数构造函数
     */
    public Lc4jPlatformConfig(String platformType, String baseUrl, String apiKey, 
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
    
    /**
     * 检查 API Key 是否已配置
     * @return true-已配置，false-未配置
     */
    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
    
    /**
     * 检查 Base URL 是否已配置
     * @return true-已配置，false-未配置
     */
    public boolean isBaseUrlConfigured() {
        return baseUrl != null && !baseUrl.trim().isEmpty();
    }
}
