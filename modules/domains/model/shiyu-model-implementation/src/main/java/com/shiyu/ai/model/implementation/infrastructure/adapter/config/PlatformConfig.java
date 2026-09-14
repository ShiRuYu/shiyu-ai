package com.shiyu.ai.model.implementation.infrastructure.adapter.config;

import lombok.Data;

/**
 * {@code PlatformConfig} 提供模型模块的配置项，并集中声明其默认值和运行约束。
 */
@Data
public class PlatformConfig {

    /**
     * platformType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String platformType;
    /**
     * adapterType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String adapterType = "OPENAI_COMPATIBLE";
    /**
     * baseUrl 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String baseUrl;
    /**
     * apiKey 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String apiKey;
    /**
     * modelName 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String modelName;
    /**
     * temperature 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double temperature = 0.7;
    /**
     * maxTokens 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer maxTokens = 4096;
    /**
     * maxRetries 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer maxRetries = 3;

    /**
     * {@code PlatformConfig} 创建并初始化当前类型实例。
     */
    public PlatformConfig() {}

    /**
     * {@code PlatformConfig} 创建并初始化当前类型实例。
     *
     * @param platformType 参数值，用于执行当前操作。
     * @param baseUrl 参数值，用于执行当前操作。
     * @param apiKey 参数值，用于执行当前操作。
     * @param modelName 参数值，用于执行当前操作。
     * @param temperature 参数值，用于执行当前操作。
     * @param maxTokens 参数值，用于执行当前操作。
     * @param maxRetries 参数值，用于执行当前操作。
     */
    public PlatformConfig(
            String platformType,
            String baseUrl,
            String apiKey,
            String modelName,
            Double temperature,
            Integer maxTokens,
            Integer maxRetries) {
        this(
                platformType,
                "OLLAMA".equalsIgnoreCase(platformType) ? "OLLAMA" : "OPENAI_COMPATIBLE",
                baseUrl,
                apiKey,
                modelName,
                temperature,
                maxTokens,
                maxRetries);
    }

    /**
     * {@code PlatformConfig} 创建并初始化当前类型实例。
     *
     * @param platformType 参数值，用于执行当前操作。
     * @param adapterType 参数值，用于执行当前操作。
     * @param baseUrl 参数值，用于执行当前操作。
     * @param apiKey 参数值，用于执行当前操作。
     * @param modelName 参数值，用于执行当前操作。
     * @param temperature 参数值，用于执行当前操作。
     * @param maxTokens 参数值，用于执行当前操作。
     * @param maxRetries 参数值，用于执行当前操作。
     */
    public PlatformConfig(
            String platformType,
            String adapterType,
            String baseUrl,
            String apiKey,
            String modelName,
            Double temperature,
            Integer maxTokens,
            Integer maxRetries) {
        this.platformType = platformType;
        this.adapterType = adapterType;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.maxRetries = maxRetries;
    }

    /**
     * {@code isApiKeyConfigured} 校验当前操作的输入或状态是否满足约束。
     *
     * @return 返回当前操作产生的结果。
     */
    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    /**
     * {@code isBaseUrlConfigured} 校验当前操作的输入或状态是否满足约束。
     *
     * @return 返回当前操作产生的结果。
     */
    public boolean isBaseUrlConfigured() {
        return baseUrl != null && !baseUrl.trim().isEmpty();
    }
}
