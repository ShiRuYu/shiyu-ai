package com.shiyu.ai.core.langchain4j;

import com.shiyu.ai.core.langchain4j.config.Lc4jPlatformConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import lombok.extern.slf4j.Slf4j;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;

/**
 * 抽象 LangChain4j 平台适配器基类
 * 提供通用的模型缓存、配置验证等功能
 */
@Slf4j
public abstract class AbstractLc4jPlatformAdapter implements Lc4jPlatformAdapter {
    
    /**
     * 同步模型缓存（按 modelName 缓存，Caffeine 带 TTL）
     */
    private final Cache<String, ChatModel> chatModelCache = Caffeine.newBuilder()
            .maximumSize(50)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();
    
    /**
     * 流式模型缓存（按 modelName 缓存，Caffeine 带 TTL）
     */
    private final Cache<String, StreamingChatModel> streamingModelCache = Caffeine.newBuilder()
            .maximumSize(50)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();
    
    /**
     * 检查 API Key 是否已配置
     * @param apiKey API Key
     * @return true-已配置，false-未配置
     */
    protected boolean isApiKeyConfigured(String apiKey) {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
    
    /**
     * 检查 Base URL 是否已配置
     * @param baseUrl Base URL
     * @return true-已配置，false-未配置
     */
    protected boolean isBaseUrlConfigured(String baseUrl) {
        return baseUrl != null && !baseUrl.trim().isEmpty();
    }
    
    /**
     * 获取或创建同步模型实例（使用默认配置）
     * @param modelName 模型名称
     * @return ChatModel 实例
     */
    @Override
    public ChatModel getChatModel(String modelName) {
        if (modelName == null || modelName.trim().isEmpty()) {
            modelName = getDefaultModelName();
        }
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new IllegalStateException(getPlatformType() + " 平台未配置默认模型");
        }
        
        final String fn = modelName;
        return chatModelCache.get(fn, k -> {
            log.debug("创建新的同步模型实例：{} - {}", getPlatformType(), fn);
            return createChatModel(fn);
        });
    }
    
    /**
     * 根据动态配置创建同步模型实例
     * @param config 平台配置
     * @param modelName 模型名称
     * @return ChatModel 实例
     */
    public ChatModel createChatModel(Lc4jPlatformConfig config, String modelName) {
        if (config == null) {
            log.warn("平台配置为 null，使用默认配置创建模型");
            return createChatModel(modelName);
        }
        
        if (!validateConfig(config)) {
            throw new IllegalStateException(getPlatformType() + " 平台配置验证失败");
        }
        
        log.debug("使用动态配置创建同步模型：{} - {}", getPlatformType(), modelName);
        return createChatModelWithConfig(config, modelName);
    }
    
    /**
     * 获取或创建流式模型实例（使用默认配置）
     * @param modelName 模型名称
     * @return StreamingChatModel 实例
     */
    @Override
    public StreamingChatModel getStreamingChatModel(String modelName) {
        if (modelName == null || modelName.trim().isEmpty() || "default".equals(modelName)) {
            modelName = getDefaultModelName();
        }
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new IllegalStateException(getPlatformType() + " 平台未配置默认模型");
        }
        
        final String fn = modelName;
        return streamingModelCache.get(fn, k -> {
            log.debug("创建新的流式模型实例：{} - {}", getPlatformType(), fn);
            return createStreamingChatModel(fn);
        });
    }
    
    /**
     * 根据动态配置创建流式模型实例
     * @param config 平台配置
     * @param modelName 模型名称
     * @return StreamingChatModel 实例
     */
    public StreamingChatModel createStreamingChatModel(Lc4jPlatformConfig config, String modelName) {
        if (config == null) {
            log.warn("平台配置为 null，使用默认配置创建模型");
            return createStreamingChatModel(modelName);
        }
        
        if (!validateConfig(config)) {
            throw new IllegalStateException(getPlatformType() + " 平台配置验证失败");
        }
        
        log.debug("使用动态配置创建流式模型：{} - {}", getPlatformType(), modelName);
        return createStreamingChatModelWithConfig(config, modelName);
    }
    
    /**
     * 创建同步模型实例（由子类实现，使用默认配置）
     * @param modelName 模型名称
     * @return ChatModel 实例
     */
    protected abstract ChatModel createChatModel(String modelName);
    
    /**
     * 创建流式模型实例（由子类实现，使用默认配置）
     * @param modelName 模型名称
     * @return StreamingChatModel 实例
     */
    protected abstract StreamingChatModel createStreamingChatModel(String modelName);
    
    /**
     * 使用动态配置创建同步模型实例（由子类实现）
     * @param config 平台配置
     * @param modelName 模型名称
     * @return ChatModel 实例
     */
    protected abstract ChatModel createChatModelWithConfig(Lc4jPlatformConfig config, String modelName);
    
    /**
     * 使用动态配置创建流式模型实例（由子类实现）
     * @param config 平台配置
     * @param modelName 模型名称
     * @return StreamingChatModel 实例
     */
    protected abstract StreamingChatModel createStreamingChatModelWithConfig(Lc4jPlatformConfig config, String modelName);
    
    /**
     * 验证平台配置
     * @param config 平台配置
     * @return true-验证通过，false-验证失败
     */
    protected boolean validateConfig(Lc4jPlatformConfig config) {
        // 检查平台类型是否匹配
        if (!getPlatformType().equals(config.getPlatformType())) {
            log.error("平台类型不匹配：期望={},实际={}", getPlatformType(), config.getPlatformType());
            return false;
        }
        
        // 基本验证逻辑，子类可以重写此方法添加更多验证
        return true;
    }
    
    /**
     * 清空模型缓存
     * 用于配置变更时刷新缓存
     */
    public void clearCache() {
        log.info("清空模型缓存：{}", getPlatformType());
        chatModelCache.invalidateAll();
        streamingModelCache.invalidateAll();
    }
}
