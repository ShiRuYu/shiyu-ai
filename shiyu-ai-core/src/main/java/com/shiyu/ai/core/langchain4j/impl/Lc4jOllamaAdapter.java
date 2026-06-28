package com.shiyu.ai.core.langchain4j.impl;

import com.shiyu.ai.core.langchain4j.AbstractLc4jPlatformAdapter;
import com.shiyu.ai.core.langchain4j.config.Lc4jPlatformConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import lombok.extern.slf4j.Slf4j;

/**
 * Ollama 平台 LangChain4j 适配器
 * 支持本地部署的大模型，不再依赖 PlatformProperties，配置由构造函数注入
 */
@Slf4j
public class Lc4jOllamaAdapter extends AbstractLc4jPlatformAdapter {

    private final String baseUrl;
    private final String defaultModel;
    private final double temperature;
    private final int maxRetries;

    public Lc4jOllamaAdapter(String baseUrl, String defaultModel, Double temperature, Integer maxRetries) {
        this.baseUrl = baseUrl != null ? baseUrl : "http://localhost:11434";
        this.defaultModel = defaultModel != null ? defaultModel : "gemma3:4b";
        this.temperature = temperature != null ? temperature : 0.7;
        this.maxRetries = maxRetries != null ? maxRetries : 3;
        log.info("Ollama Adapter 初始化成功，baseUrl: {}, defaultModel: {}", this.baseUrl, this.defaultModel);
    }

    @Override
    public String getPlatformType() {
        return "OLLAMA";
    }

    @Override
    protected ChatModel createChatModel(String modelName) {
        if (!isBaseUrlConfigured(baseUrl)) {
            log.warn("Ollama Base URL 未配置");
            return null;
        }
        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .maxRetries(maxRetries)
                .build();
    }

    @Override
    protected StreamingChatModel createStreamingChatModel(String modelName) {
        if (!isBaseUrlConfigured(baseUrl)) {
            log.warn("Ollama Base URL 未配置");
            return null;
        }
        return OllamaStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .build();
    }

    @Override
    public String getDefaultModelName() {
        return defaultModel;
    }

    @Override
    public boolean isAvailable() {
        return isBaseUrlConfigured(baseUrl);
    }

    @Override
    protected ChatModel createChatModelWithConfig(Lc4jPlatformConfig config, String modelName) {
        if (!config.isBaseUrlConfigured()) {
            log.warn("Ollama Base URL 未配置");
            return null;
        }
        return OllamaChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .modelName(modelName)
                .temperature(config.getTemperature())
                .maxRetries(config.getMaxRetries())
                .build();
    }

    @Override
    protected StreamingChatModel createStreamingChatModelWithConfig(Lc4jPlatformConfig config, String modelName) {
        if (!config.isBaseUrlConfigured()) {
            log.warn("Ollama Base URL 未配置");
            return null;
        }
        return OllamaStreamingChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .modelName(modelName)
                .temperature(config.getTemperature())
                .build();
    }
}
