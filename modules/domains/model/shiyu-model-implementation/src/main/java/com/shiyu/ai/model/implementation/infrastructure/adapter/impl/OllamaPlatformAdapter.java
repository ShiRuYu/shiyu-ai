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
 * {@code OllamaPlatformAdapter} 承载模型模块的领域状态或协作行为，负责维护本类型的职责边界。
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
     * {@code OllamaPlatformAdapter} 创建并初始化当前类型实例。
     *
     * @param baseUrl 参数值，用于执行当前操作。
     * @param defaultModel 参数值，用于执行当前操作。
     * @param temperature 参数值，用于执行当前操作。
     * @param maxRetries 参数值，用于执行当前操作。
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
     * {@code getPlatformType} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public String getPlatformType() {
        return "OLLAMA";
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
     * {@code createStreamingChatModel} 写入或更新当前模块中的业务数据。
     *
     * @param modelName 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
        return isBaseUrlConfigured(baseUrl);
    }

    /**
     * {@code validateConfig} 校验当前操作的输入或状态是否满足约束。
     *
     * @param config 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    protected boolean validateConfig(PlatformConfig config) {
        return PlatformAdapterType.OLLAMA == PlatformAdapterType.parse(config.getAdapterType());
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
