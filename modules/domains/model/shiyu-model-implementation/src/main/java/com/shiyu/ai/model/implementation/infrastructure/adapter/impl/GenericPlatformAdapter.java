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
 * 将 Generic 平台 在不同层之间进行适配、转换或组装。
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
     * 执行 Generic 平台 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param platformType 用于完成本次业务处理的 platformType 参数。
     * @param baseUrl 用于完成本次业务处理的 baseUrl 参数。
     * @param apiKey 用于完成本次业务处理的 apiKey 参数。
     * @param defaultModel 用于完成本次业务处理的 defaultModel 参数。
     */
    public GenericPlatformAdapter(
            String platformType, String baseUrl, String apiKey, String defaultModel) {
        this(platformType, baseUrl, apiKey, defaultModel, 3);
    }

    /**
     * 执行 Generic 平台 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param platformType 用于完成本次业务处理的 platformType 参数。
     * @param baseUrl 用于完成本次业务处理的 baseUrl 参数。
     * @param apiKey 用于完成本次业务处理的 apiKey 参数。
     * @param defaultModel 用于完成本次业务处理的 defaultModel 参数。
     * @param maxRetries 用于完成本次业务处理的 maxRetries 参数。
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
     * 查询 Generic 平台 相关业务数据，并返回处理结果。
     *
     * @return 返回 Generic 平台 相关操作生成的结果数据。
     */
    @Override
    public String getPlatformType() {
        return platformType;
    }

    /**
     * 创建或保存 Generic 平台 相关业务数据，并返回处理结果。
     *
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @return 返回 Generic 平台 相关操作生成的结果数据。
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
     * 创建或保存 Generic 平台 相关业务数据，并返回处理结果。
     *
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @return 返回 Generic 平台 相关操作生成的结果数据。
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
     * 查询 Generic 平台 相关业务数据，并返回处理结果。
     *
     * @return 返回 Generic 平台 相关操作生成的结果数据。
     */
    @Override
    public String getDefaultModelName() {
        return defaultModel;
    }

    /**
     * 校验或判断 Generic 平台 相关业务数据，并返回处理结果。
     *
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean isAvailable() {
        return isApiKeyConfigured(apiKey);
    }

    /**
     * 创建或保存 Generic 平台 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @return 返回 Generic 平台 相关操作生成的结果数据。
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
     * 创建或保存 Generic 平台 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @return 返回 Generic 平台 相关操作生成的结果数据。
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
