package com.shiyu.ai.core.langchain4j;

import com.shiyu.ai.core.langchain4j.config.PlatformConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import lombok.extern.slf4j.Slf4j;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractModelAdapter implements ModelAdapter {

    private final Cache<String, ChatModel> chatModelCache = Caffeine.newBuilder()
            .maximumSize(50)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    private final Cache<String, StreamingChatModel> streamingModelCache = Caffeine.newBuilder()
            .maximumSize(50)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    protected boolean isApiKeyConfigured(String apiKey) {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    protected boolean isBaseUrlConfigured(String baseUrl) {
        return baseUrl != null && !baseUrl.trim().isEmpty();
    }

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

    public ChatModel createChatModel(PlatformConfig config, String modelName) {
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

    public StreamingChatModel createStreamingChatModel(PlatformConfig config, String modelName) {
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

    protected abstract ChatModel createChatModel(String modelName);

    protected abstract StreamingChatModel createStreamingChatModel(String modelName);

    protected abstract ChatModel createChatModelWithConfig(PlatformConfig config, String modelName);

    protected abstract StreamingChatModel createStreamingChatModelWithConfig(PlatformConfig config, String modelName);

    protected boolean validateConfig(PlatformConfig config) {
        if (!getPlatformType().equals(config.getPlatformType())) {
            log.error("平台类型不匹配：期望={},实际={}", getPlatformType(), config.getPlatformType());
            return false;
        }
        return true;
    }

    public void clearCache() {
        log.info("清空模型缓存：{}", getPlatformType());
        chatModelCache.invalidateAll();
        streamingModelCache.invalidateAll();
    }
}
