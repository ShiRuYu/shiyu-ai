package com.shiyu.ai.model.adapter.impl;

import com.shiyu.ai.model.adapter.AbstractModelAdapter;
import com.shiyu.ai.model.adapter.config.PlatformConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenericPlatformAdapter extends AbstractModelAdapter {

    private final String platformType;
    private final String baseUrl;
    private final String apiKey;
    private final String defaultModel;

    public GenericPlatformAdapter(String platformType, String baseUrl, String apiKey, String defaultModel) {
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
                .temperature(0.7).maxTokens(4096).returnThinking(true).build();
    }

    @Override
    protected StreamingChatModel createStreamingChatModel(String modelName) {
        if (!isApiKeyConfigured(apiKey)) {
            log.warn("{} API Key 未配置", platformType);
            return null;
        }
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl).apiKey(apiKey).modelName(modelName)
                .temperature(0.7).maxTokens(4096).returnThinking(true).build();
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
    protected ChatModel createChatModelWithConfig(PlatformConfig config, String modelName) {
        if (!config.isApiKeyConfigured()) {
            log.warn("{} API Key 未配置", platformType);
            return null;
        }
        return OpenAiChatModel.builder()
                .baseUrl(config.getBaseUrl()).apiKey(config.getApiKey()).modelName(modelName)
                .temperature(config.getTemperature()).maxTokens(config.getMaxTokens()).returnThinking(true).build();
    }

    @Override
    protected StreamingChatModel createStreamingChatModelWithConfig(PlatformConfig config, String modelName) {
        if (!config.isApiKeyConfigured()) {
            log.warn("{} API Key 未配置", platformType);
            return null;
        }
        return OpenAiStreamingChatModel.builder()
                .baseUrl(config.getBaseUrl()).apiKey(config.getApiKey()).modelName(modelName)
                .temperature(config.getTemperature()).maxTokens(config.getMaxTokens()).returnThinking(true).build();
    }
}
