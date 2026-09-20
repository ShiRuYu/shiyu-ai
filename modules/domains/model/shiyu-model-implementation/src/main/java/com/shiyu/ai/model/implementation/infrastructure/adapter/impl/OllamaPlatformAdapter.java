package com.shiyu.ai.model.implementation.infrastructure.adapter.impl;

import com.shiyu.ai.model.implementation.domain.model.PlatformAdapterType;
import com.shiyu.ai.model.implementation.infrastructure.adapter.AbstractModelAdapter;
import com.shiyu.ai.model.implementation.infrastructure.adapter.config.PlatformConfig;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;

import lombok.extern.slf4j.Slf4j;

/**
 * 将 Ollama 平台 在不同层之间进行适配、转换或组装。
 */
@Slf4j
public class OllamaPlatformAdapter extends AbstractModelAdapter {

    /**
     * baseUrl 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String baseUrl;
    /**
     * defaultModel 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String defaultModel;
    /**
     * temperature 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final double temperature;
    /**
     * maxRetries 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final int maxRetries;

    /**
     * 执行 Ollama 平台 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param baseUrl 用于完成本次业务处理的 baseUrl 参数。
     * @param defaultModel 用于完成本次业务处理的 defaultModel 参数。
     * @param temperature 用于完成本次业务处理的 temperature 参数。
     * @param maxRetries 用于完成本次业务处理的 maxRetries 参数。
     */
    public OllamaPlatformAdapter(
            String baseUrl, String defaultModel, Double temperature, Integer maxRetries) {
        this.baseUrl = baseUrl != null ? baseUrl : "http://localhost:11434";
        this.defaultModel = defaultModel != null ? defaultModel : "gemma3:4b";
        this.temperature = temperature != null ? temperature : 0.7;
        this.maxRetries = maxRetries != null ? maxRetries : 3;
        log.info(
                "Ollama Adapter 初始化成功，baseUrl: {}, defaultModel: {}",
                this.baseUrl,
                this.defaultModel);
    }

    /**
     * 查询 Ollama 平台 相关业务数据，并返回处理结果。
     *
     * @return 返回 Ollama 平台 相关操作生成的结果数据。
     */
    @Override
    public String getPlatformType() {
        return "OLLAMA";
    }

    /**
     * 创建或保存 Ollama 平台 相关业务数据，并返回处理结果。
     *
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @return 返回 Ollama 平台 相关操作生成的结果数据。
     */
    @Override
    protected ChatModel createChatModel(String modelName) {
        if (!isBaseUrlConfigured(baseUrl)) {
            log.warn("Ollama Base URL 未配置");
            return null;
        }
        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .maxRetries(maxRetries)
                .build();
    }

    /**
     * 创建或保存 Ollama 平台 相关业务数据，并返回处理结果。
     *
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @return 返回 Ollama 平台 相关操作生成的结果数据。
     */
    @Override
    protected StreamingChatModel createStreamingChatModel(String modelName) {
        if (!isBaseUrlConfigured(baseUrl)) {
            log.warn("Ollama Base URL 未配置");
            return null;
        }
        return OllamaStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .build();
    }

    /**
     * 查询 Ollama 平台 相关业务数据，并返回处理结果。
     *
     * @return 返回 Ollama 平台 相关操作生成的结果数据。
     */
    @Override
    public String getDefaultModelName() {
        return defaultModel;
    }

    /**
     * 校验或判断 Ollama 平台 相关业务数据，并返回处理结果。
     *
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean isAvailable() {
        return isBaseUrlConfigured(baseUrl);
    }

    /**
     * 校验或判断 Ollama 平台 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @return 返回本次条件判断是否成立。
     */
    @Override
    protected boolean validateConfig(PlatformConfig config) {
        return PlatformAdapterType.OLLAMA == PlatformAdapterType.parse(config.getAdapterType());
    }

    /**
     * 创建或保存 Ollama 平台 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @return 返回 Ollama 平台 相关操作生成的结果数据。
     */
    @Override
    protected ChatModel createChatModelWithConfig(PlatformConfig config, String modelName) {
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

    /**
     * 创建或保存 Ollama 平台 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @return 返回 Ollama 平台 相关操作生成的结果数据。
     */
    @Override
    protected StreamingChatModel createStreamingChatModelWithConfig(
            PlatformConfig config, String modelName) {
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
