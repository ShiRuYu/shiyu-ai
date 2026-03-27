package com.shiyu.ai.agent.langchain4j.impl;

import com.shiyu.ai.agent.config.PlatformProperties;
import com.shiyu.ai.agent.langchain4j.AbstractLc4jPlatformAdapter;
import com.shiyu.ai.agent.langchain4j.config.Lc4jPlatformConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Ollama 平台 LangChain4j 适配器
 * 支持本地部署的大模型
 */
@Slf4j
@Component("lc4jOllamaAdapter")
public class Lc4jOllamaAdapter extends AbstractLc4jPlatformAdapter {
    
    private final PlatformProperties.OllamaConfig defaultConfig;
    
    public Lc4jOllamaAdapter(PlatformProperties platformProperties) {
        this.defaultConfig = platformProperties.getOllama();
        log.info("LangChain4j Ollama Adapter 初始化成功，baseUrl: {}", defaultConfig.getBaseUrl());
    }
    
    @Override
    public String getPlatformType() {
        return "OLLAMA";
    }
    
    @Override
    protected ChatModel createChatModel(String modelName) {
        if (!isBaseUrlConfigured(defaultConfig.getBaseUrl())) {
            log.warn("Ollama Base URL 未配置");
            return null;
        }
        
        return OllamaChatModel.builder()
                .baseUrl(defaultConfig.getBaseUrl())
                .modelName(modelName)
                .temperature(0.7)
                .maxRetries(3)
                .build();
    }
    
    @Override
    protected StreamingChatModel createStreamingChatModel(String modelName) {
        if (!isBaseUrlConfigured(defaultConfig.getBaseUrl())) {
            log.warn("Ollama Base URL 未配置");
            return null;
        }
        
        return OllamaStreamingChatModel.builder()
                .baseUrl(defaultConfig.getBaseUrl())
                .modelName(modelName)
                .temperature(0.7)
                .build();
    }
    
    @Override
    public String getDefaultModelName() {
        return defaultConfig.getModel();
    }
    
    @Override
    public boolean isAvailable() {
        return isBaseUrlConfigured(defaultConfig.getBaseUrl());
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
