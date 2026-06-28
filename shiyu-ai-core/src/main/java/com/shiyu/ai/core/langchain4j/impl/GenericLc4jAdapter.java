package com.shiyu.ai.core.langchain4j.impl;

import com.shiyu.ai.core.langchain4j.AbstractLc4jPlatformAdapter;
import com.shiyu.ai.core.langchain4j.config.Lc4jPlatformConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenericLc4jAdapter extends AbstractLc4jPlatformAdapter {

    private final String platformType;
    private final String baseUrl;
    private final String apiKey;
    private final String defaultModel;

    public GenericLc4jAdapter(String platformType, String baseUrl, String apiKey, String defaultModel) {
        this.platformType = platformType;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.defaultModel = defaultModel;
        log.info("{} Adapter 初始化成功，baseUrl: {}", platformType, baseUrl);
    }

    @Override
    public String getPlatformType() {
        return platformType;
    }

    @Override
    protected ChatModel createChatModel(String modelName) {
        if (!isApiKeyConfigured(apiKey)) {
            log.warn("{} API Key 未配置", platformType);
            return null;
        }
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl).apiKey(apiKey).modelName(modelName)
                .temperature(0.7).maxTokens(4096).build();
    }

    @Override
    protected StreamingChatModel createStreamingChatModel(String modelName) {
        if (!isApiKeyConfigured(apiKey)) {
            log.warn("{} API Key 未配置", platformType);
            return null;
        }
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl).apiKey(apiKey).modelName(modelName)
                .temperature(0.7).maxTokens(4096).build();
    }

    @Override
    public String getDefaultModelName() {
        return defaultModel;
    }

    @Override
    public boolean isAvailable() {
        return isApiKeyConfigured(apiKey);
    }

    @Override
    protected ChatModel createChatModelWithConfig(Lc4jPlatformConfig config, String modelName) {
        if (!config.isApiKeyConfigured()) {
            log.warn("{} API Key 未配置", platformType);
            return null;
        }
        return OpenAiChatModel.builder()
                .baseUrl(config.getBaseUrl()).apiKey(config.getApiKey()).modelName(modelName)
                .temperature(config.getTemperature()).maxTokens(config.getMaxTokens()).build();
    }

    @Override
    protected StreamingChatModel createStreamingChatModelWithConfig(Lc4jPlatformConfig config, String modelName) {
        if (!config.isApiKeyConfigured()) {
            log.warn("{} API Key 未配置", platformType);
            return null;
        }
        return OpenAiStreamingChatModel.builder()
                .baseUrl(config.getBaseUrl()).apiKey(config.getApiKey()).modelName(modelName)
                .temperature(config.getTemperature()).maxTokens(config.getMaxTokens()).build();
    }
}