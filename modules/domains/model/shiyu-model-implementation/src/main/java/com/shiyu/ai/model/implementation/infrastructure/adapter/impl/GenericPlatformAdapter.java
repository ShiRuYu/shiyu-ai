package com.shiyu.ai.model.implementation.infrastructure.adapter.impl;

import com.shiyu.ai.model.implementation.infrastructure.adapter.AbstractModelAdapter;
import com.shiyu.ai.model.implementation.infrastructure.adapter.config.PlatformConfig;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * {@code GenericPlatformAdapter} 承载模型模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Slf4j
public class GenericPlatformAdapter extends AbstractModelAdapter {

    /**
     * platformType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String platformType;
    /**
     * baseUrl 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String baseUrl;
    /**
     * apiKey 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String apiKey;
    /**
     * defaultModel 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String defaultModel;
    /**
     * maxRetries 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final int maxRetries;

    /**
     * {@code GenericPlatformAdapter} 创建并初始化当前类型实例。
     *
     * @param platformType 参数值，用于执行当前操作。
     * @param baseUrl 参数值，用于执行当前操作。
     * @param apiKey 参数值，用于执行当前操作。
     * @param defaultModel 参数值，用于执行当前操作。
     */
    public GenericPlatformAdapter(
            String platformType, String baseUrl, String apiKey, String defaultModel) {
        this(platformType, baseUrl, apiKey, defaultModel, 3);
    }

    /**
     * {@code GenericPlatformAdapter} 创建并初始化当前类型实例。
     *
     * @param platformType 参数值，用于执行当前操作。
     * @param baseUrl 参数值，用于执行当前操作。
     * @param apiKey 参数值，用于执行当前操作。
     * @param defaultModel 参数值，用于执行当前操作。
     * @param maxRetries 参数值，用于执行当前操作。
     */
    public GenericPlatformAdapter(
            String platformType,
            String baseUrl,
            String apiKey,
            String defaultModel,
            int maxRetries) {
        this.platformType = platformType;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.defaultModel = defaultModel;
        this.maxRetries = Math.max(0, Math.min(maxRetries, 8));
        log.info("{} Adapter 初始化成功，baseUrl: {}", platformType, baseUrl);
    }

    /**
     * {@code getPlatformType} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public String getPlatformType() {
        return platformType;
    }

    /**
     * {@code createChatModel} 写入或更新当前模块中的业务数据。
     *
     * @param modelName 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    protected ChatModel createChatModel(String modelName) {
        if (!isApiKeyConfigured(apiKey)) {
            log.warn("{} API Key 未配置", platformType);
            return null;
        }
        OpenAiChatModel.OpenAiChatModelBuilder builder =
                OpenAiChatModel.builder()
                        .baseUrl(baseUrl)
                        .apiKey(apiKey)
                        .modelName(modelName)
                        .temperature(0.7)
                        .maxTokens(4096)
                        .returnThinking(true)
                        .timeout(Duration.ofSeconds(60))
                        .maxRetries(maxRetries)
                        .parallelToolCalls(true);
        if ("DEEPSEEK".equalsIgnoreCase(platformType)) builder.reasoningEffort("medium");
        return builder.build();
    }

    /**
     * {@code createStreamingChatModel} 写入或更新当前模块中的业务数据。
     *
     * @param modelName 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    protected StreamingChatModel createStreamingChatModel(String modelName) {
        if (!isApiKeyConfigured(apiKey)) {
            log.warn("{} API Key 未配置", platformType);
            return null;
        }
        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder =
                OpenAiStreamingChatModel.builder()
                        .baseUrl(baseUrl)
                        .apiKey(apiKey)
                        .modelName(modelName)
                        .temperature(0.7)
                        .maxTokens(4096)
                        .returnThinking(true)
                        .timeout(Duration.ofSeconds(60))
                        .accumulateToolCallId(true)
                        .parallelToolCalls(true);
        if ("DEEPSEEK".equalsIgnoreCase(platformType)) builder.reasoningEffort("medium");
        return builder.build();
    }

    /**
     * {@code getDefaultModelName} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public String getDefaultModelName() {
        return defaultModel;
    }

    /**
     * {@code isAvailable} 校验当前操作的输入或状态是否满足约束。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public boolean isAvailable() {
        return isApiKeyConfigured(apiKey);
    }

    /**
     * {@code createChatModelWithConfig} 写入或更新当前模块中的业务数据。
     *
     * @param config 参数值，用于执行当前操作。
     * @param modelName 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    protected ChatModel createChatModelWithConfig(PlatformConfig config, String modelName) {
        if (!config.isApiKeyConfigured()) {
            log.warn("{} API Key 未配置", platformType);
            return null;
        }
        OpenAiChatModel.OpenAiChatModelBuilder builder =
                OpenAiChatModel.builder()
                        .baseUrl(config.getBaseUrl())
                        .apiKey(config.getApiKey())
                        .modelName(modelName)
                        .temperature(config.getTemperature())
                        .maxTokens(config.getMaxTokens())
                        .returnThinking(true)
                        .timeout(Duration.ofSeconds(60))
                        .maxRetries(
                                config.getMaxRetries() == null
                                        ? maxRetries
                                        : Math.max(0, Math.min(config.getMaxRetries(), 8)))
                        .parallelToolCalls(true);
        if ("DEEPSEEK".equalsIgnoreCase(platformType)) builder.reasoningEffort("medium");
        return builder.build();
    }

    /**
     * {@code createStreamingChatModelWithConfig} 写入或更新当前模块中的业务数据。
     *
     * @param config 参数值，用于执行当前操作。
     * @param modelName 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    protected StreamingChatModel createStreamingChatModelWithConfig(
            PlatformConfig config, String modelName) {
        if (!config.isApiKeyConfigured()) {
            log.warn("{} API Key 未配置", platformType);
            return null;
        }
        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder =
                OpenAiStreamingChatModel.builder()
                        .baseUrl(config.getBaseUrl())
                        .apiKey(config.getApiKey())
                        .modelName(modelName)
                        .temperature(config.getTemperature())
                        .maxTokens(config.getMaxTokens())
                        .returnThinking(true)
                        .timeout(Duration.ofSeconds(60))
                        .accumulateToolCallId(true)
                        .parallelToolCalls(true);
        if ("DEEPSEEK".equalsIgnoreCase(platformType)) builder.reasoningEffort("medium");
        return builder.build();
    }
}
