package com.shiyu.ai.agent.langchain4j.impl;

import com.shiyu.ai.agent.agent.config.PlatformProperties;
import com.shiyu.ai.agent.langchain4j.AbstractLc4jPlatformAdapter;
import com.shiyu.ai.agent.langchain4j.config.Lc4jPlatformConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * OpenRouter 平台 LangChain4j 适配器
 * OpenRouter 提供统一接口访问多个模型提供商
 */
@Slf4j
@Component("lc4jOpenRouterAdapter")
public class Lc4jOpenRouterAdapter extends AbstractLc4jPlatformAdapter {
    
    private final PlatformProperties.OpenRouterConfig defaultConfig;
    
    public Lc4jOpenRouterAdapter(PlatformProperties platformProperties) {
        this.defaultConfig = platformProperties.getOpenrouter();
        log.info("LangChain4j OpenRouter Adapter 初始化成功，baseUrl: {}", defaultConfig.getBaseUrl());
    }
    
    @Override
    public String getPlatformType() {
        return "OPENROUTER";
    }
    
    @Override
    protected ChatModel createChatModel(String modelName) {
        if (!isApiKeyConfigured(defaultConfig.getApiKey())) {
            log.warn("OpenRouter API Key 未配置，返回 Mock 模型");
            return null;
        }
        
        // OpenRouter 使用 OpenAI 兼容协议
        return OpenAiChatModel.builder()
                .baseUrl(defaultConfig.getBaseUrl())
                .apiKey(defaultConfig.getApiKey())
                .modelName(modelName)
                .temperature(0.7)
                .maxTokens(4096)
                .build();
    }
    
    @Override
    protected StreamingChatModel createStreamingChatModel(String modelName) {
        if (!isApiKeyConfigured(defaultConfig.getApiKey())) {
            log.warn("OpenRouter API Key 未配置，返回 Mock 流式模型");
            return null;
        }
        
        return OpenAiStreamingChatModel.builder()
                .baseUrl(defaultConfig.getBaseUrl())
                .apiKey(defaultConfig.getApiKey())
                .modelName(modelName)
                .temperature(0.7)
                .maxTokens(4096)
                .build();
    }
    
    @Override
    public String getDefaultModelName() {
        return defaultConfig.getModel();
    }
    
    @Override
    public boolean isAvailable() {
        return isApiKeyConfigured(defaultConfig.getApiKey());
    }
    
    @Override
    protected ChatModel createChatModelWithConfig(Lc4jPlatformConfig config, String modelName) {
        if (!config.isApiKeyConfigured()) {
            log.warn("OpenRouter API Key 未配置，返回 Mock 模型");
            return null;
        }
        
        // OpenRouter 使用 OpenAI 兼容协议
        return OpenAiChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(modelName)
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .build();
    }
    
    @Override
    protected StreamingChatModel createStreamingChatModelWithConfig(Lc4jPlatformConfig config, String modelName) {
        if (!config.isApiKeyConfigured()) {
            log.warn("OpenRouter API Key 未配置，返回 Mock 流式模型");
            return null;
        }
        
        return OpenAiStreamingChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(modelName)
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .build();
    }
}
