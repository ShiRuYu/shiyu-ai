package com.shiyu.ai.agent.langchain4j.impl;

import com.shiyu.ai.agent.config.PlatformProperties;
import com.shiyu.ai.agent.langchain4j.AbstractLc4jPlatformAdapter;
import com.shiyu.ai.agent.langchain4j.config.Lc4jPlatformConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * DeepSeek 平台 LangChain4j 适配器
 * DeepSeek 提供高性能的中文对话模型
 */
@Slf4j
@Component("lc4jDeepSeekAdapter")
public class Lc4jDeepSeekAdapter extends AbstractLc4jPlatformAdapter {
    
    private final PlatformProperties.DeepSeekConfig defaultConfig;
    
    public Lc4jDeepSeekAdapter(PlatformProperties platformProperties) {
        this.defaultConfig = platformProperties.getDeepseek();
        log.info("LangChain4j DeepSeek Adapter 初始化成功，baseUrl: {}", defaultConfig.getBaseUrl());
    }
    
    @Override
    public String getPlatformType() {
        return "DEEPSEEK";
    }
    
    @Override
    protected ChatModel createChatModel(String modelName) {
        if (!isApiKeyConfigured(defaultConfig.getApiKey())) {
            log.warn("DeepSeek API Key 未配置，返回 Mock 模型");
            return null;
        }
        
        // DeepSeek 使用 OpenAI 兼容协议
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
            log.warn("DeepSeek API Key 未配置，返回 Mock 流式模型");
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
            log.warn("DeepSeek API Key 未配置，返回 Mock 模型");
            return null;
        }
        
        // DeepSeek 使用 OpenAI 兼容协议
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
            log.warn("DeepSeek API Key 未配置，返回 Mock 流式模型");
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
