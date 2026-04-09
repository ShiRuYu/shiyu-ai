package com.shiyu.ai.agent.langchain4j.impl;

import com.shiyu.ai.agent.biz.agent.config.PlatformProperties;
import com.shiyu.ai.agent.langchain4j.AbstractLc4jPlatformAdapter;
import com.shiyu.ai.agent.langchain4j.config.Lc4jPlatformConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SiliconFlow 平台 LangChain4j 适配器
 * 硅基流动提供多种开源模型的 API 服务
 */
@Slf4j
@Component("lc4jSiliconFlowAdapter")
public class Lc4jSiliconFlowAdapter extends AbstractLc4jPlatformAdapter {
    
    private final PlatformProperties.SiliconFlowConfig defaultConfig;
    
    public Lc4jSiliconFlowAdapter(PlatformProperties platformProperties) {
        this.defaultConfig = platformProperties.getSiliconflow();
        log.info("LangChain4j SiliconFlow Adapter 初始化成功，baseUrl: {}", defaultConfig.getBaseUrl());
    }
    
    @Override
    public String getPlatformType() {
        return "SILICON_FLOW";
    }
    
    @Override
    protected ChatModel createChatModel(String modelName) {
        if (!isApiKeyConfigured(defaultConfig.getApiKey())) {
            log.warn("SiliconFlow API Key 未配置，返回 Mock 模型");
            return null;
        }
        
        // SiliconFlow 使用 OpenAI 兼容协议
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
            log.warn("SiliconFlow API Key 未配置，返回 Mock 流式模型");
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
            log.warn("SiliconFlow API Key 未配置，返回 Mock 模型");
            return null;
        }
        
        // SiliconFlow 使用 OpenAI 兼容协议
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
            log.warn("SiliconFlow API Key 未配置，返回 Mock 流式模型");
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
