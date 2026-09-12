package com.shiyu.ai.model.implementation.infrastructure.adapter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shiyu.ai.model.implementation.infrastructure.adapter.config.PlatformConfig;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * {@code AbstractModelAdapter} 承载模型模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Slf4j
public abstract class AbstractModelAdapter implements ModelAdapter {

    private final Cache<String, ChatModel> chatModelCache =
            Caffeine.newBuilder().maximumSize(50).expireAfterWrite(30, TimeUnit.MINUTES).build();

    private final Cache<String, StreamingChatModel> streamingModelCache =
            Caffeine.newBuilder().maximumSize(50).expireAfterWrite(30, TimeUnit.MINUTES).build();

    /**
     * {@code isApiKeyConfigured} 校验当前操作的输入或状态是否满足约束。
     *
     * @param apiKey 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    protected boolean isApiKeyConfigured(String apiKey) {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    /**
     * {@code isBaseUrlConfigured} 校验当前操作的输入或状态是否满足约束。
     *
     * @param baseUrl 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    protected boolean isBaseUrlConfigured(String baseUrl) {
        return baseUrl != null && !baseUrl.trim().isEmpty();
    }

    /**
     * {@code getChatModel} 查询并返回当前操作所需的数据。
     *
     * @param modelName 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
        return chatModelCache.get(
                fn,
                k -> {
                    log.debug("创建新的同步模型实例：{} - {}", getPlatformType(), fn);
                    return createChatModel(fn);
                });
    }

    /**
     * {@code createChatModel} 写入或更新当前模块中的业务数据。
     *
     * @param config 参数值，用于执行当前操作。
     * @param modelName 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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

    /**
     * {@code getStreamingChatModel} 查询并返回当前操作所需的数据。
     *
     * @param modelName 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
        return streamingModelCache.get(
                fn,
                k -> {
                    log.debug("创建新的流式模型实例：{} - {}", getPlatformType(), fn);
                    return createStreamingChatModel(fn);
                });
    }

    /**
     * {@code createStreamingChatModel} 写入或更新当前模块中的业务数据。
     *
     * @param config 参数值，用于执行当前操作。
     * @param modelName 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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

    /**
     * {@code createChatModel} 写入或更新当前模块中的业务数据。
     *
     * @param modelName 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    protected abstract ChatModel createChatModel(String modelName);

    /**
     * {@code createStreamingChatModel} 写入或更新当前模块中的业务数据。
     *
     * @param modelName 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    protected abstract StreamingChatModel createStreamingChatModel(String modelName);

    /**
     * {@code createChatModelWithConfig} 写入或更新当前模块中的业务数据。
     *
     * @param config 参数值，用于执行当前操作。
     * @param modelName 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    protected abstract ChatModel createChatModelWithConfig(PlatformConfig config, String modelName);

    /**
     * {@code createStreamingChatModelWithConfig} 写入或更新当前模块中的业务数据。
     *
     * @param config 参数值，用于执行当前操作。
     * @param modelName 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    protected abstract StreamingChatModel createStreamingChatModelWithConfig(
            PlatformConfig config, String modelName);

    /**
     * {@code validateConfig} 校验当前操作的输入或状态是否满足约束。
     *
     * @param config 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    protected boolean validateConfig(PlatformConfig config) {
        if (!getPlatformType().equals(config.getPlatformType())) {
            log.error("平台类型不匹配：期望={},实际={}", getPlatformType(), config.getPlatformType());
            return false;
        }
        return true;
    }

    /**
     * {@code clearCache} 执行当前类型定义的业务操作。
     */
    public void clearCache() {
        log.info("清空模型缓存：{}", getPlatformType());
        chatModelCache.invalidateAll();
        streamingModelCache.invalidateAll();
    }
}
