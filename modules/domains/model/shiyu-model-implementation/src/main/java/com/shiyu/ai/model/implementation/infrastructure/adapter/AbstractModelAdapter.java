package com.shiyu.ai.model.implementation.infrastructure.adapter;

import com.shiyu.ai.model.implementation.infrastructure.port.ModelAdapter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shiyu.ai.model.implementation.infrastructure.adapter.config.PlatformConfig;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 将 Abstract 模型 在不同层之间进行适配、转换或组装。
 */
@Slf4j
public abstract class AbstractModelAdapter implements ModelAdapter {

    private final Cache<String, ChatModel> chatModelCache =
            Caffeine.newBuilder().maximumSize(50).expireAfterWrite(30, TimeUnit.MINUTES).build();

    private final Cache<String, StreamingChatModel> streamingModelCache =
            Caffeine.newBuilder().maximumSize(50).expireAfterWrite(30, TimeUnit.MINUTES).build();

    /**
     * 校验或判断 Abstract 模型 相关业务数据，并返回处理结果。
     *
     * @param apiKey 用于完成本次业务处理的 apiKey 参数。
     * @return 返回本次条件判断是否成立。
     */
    protected boolean isApiKeyConfigured(String apiKey) {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    /**
     * 校验或判断 Abstract 模型 相关业务数据，并返回处理结果。
     *
     * @param baseUrl 用于完成本次业务处理的 baseUrl 参数。
     * @return 返回本次条件判断是否成立。
     */
    protected boolean isBaseUrlConfigured(String baseUrl) {
        return baseUrl != null && !baseUrl.trim().isEmpty();
    }

    /**
     * 查询 Abstract 模型 相关业务数据，并返回处理结果。
     *
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @return 返回 Abstract 模型 相关操作生成的结果数据。
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
     * 创建或保存 Abstract 模型 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @return 返回 Abstract 模型 相关操作生成的结果数据。
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
     * 查询 Abstract 模型 相关业务数据，并返回处理结果。
     *
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @return 返回 Abstract 模型 相关操作生成的结果数据。
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
     * 创建或保存 Abstract 模型 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @return 返回 Abstract 模型 相关操作生成的结果数据。
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
     * 创建或保存 Abstract 模型 相关业务数据，并返回处理结果。
     *
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @return 返回 Abstract 模型 相关操作生成的结果数据。
     */
    protected abstract ChatModel createChatModel(String modelName);

    /**
     * 创建或保存 Abstract 模型 相关业务数据，并返回处理结果。
     *
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @return 返回 Abstract 模型 相关操作生成的结果数据。
     */
    protected abstract StreamingChatModel createStreamingChatModel(String modelName);

    /**
     * 创建或保存 Abstract 模型 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @return 返回 Abstract 模型 相关操作生成的结果数据。
     */
    protected abstract ChatModel createChatModelWithConfig(PlatformConfig config, String modelName);

    /**
     * 创建或保存 Abstract 模型 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @return 返回 Abstract 模型 相关操作生成的结果数据。
     */
    protected abstract StreamingChatModel createStreamingChatModelWithConfig(
            PlatformConfig config, String modelName);

    /**
     * 校验或判断 Abstract 模型 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @return 返回本次条件判断是否成立。
     */
    protected boolean validateConfig(PlatformConfig config) {
        if (!getPlatformType().equals(config.getPlatformType())) {
            log.error("平台类型不匹配：期望={},实际={}", getPlatformType(), config.getPlatformType());
            return false;
        }
        return true;
    }

    /**
     * 删除或移除 Abstract 模型 相关业务操作，并维护必要的状态和协作关系。
     */
    public void clearCache() {
        log.info("清空模型缓存：{}", getPlatformType());
        chatModelCache.invalidateAll();
        streamingModelCache.invalidateAll();
    }
}
