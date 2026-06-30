package com.shiyu.ai.core.langchain4j.impl;

import com.shiyu.ai.core.langchain4j.AbstractModelAdapter;
import com.shiyu.ai.core.langchain4j.config.PlatformConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OllamaPlatformAdapter extends AbstractModelAdapter {

    private final String baseUrl;
    private final String defaultModel;
    private final double temperature;
    private final int maxRetries;

    public OllamaPlatformAdapter(String baseUrl, String defaultModel, Double temperature, Integer maxRetries) {
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
    protected ChatModel createChatModelWithConfig(PlatformConfig config, String modelName) {
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
    protected StreamingChatModel createStreamingChatModelWithConfig(PlatformConfig config, String modelName) {
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
