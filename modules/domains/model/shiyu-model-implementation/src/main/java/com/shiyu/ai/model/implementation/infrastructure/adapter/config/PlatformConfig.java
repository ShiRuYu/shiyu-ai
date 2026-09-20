package com.shiyu.ai.model.implementation.infrastructure.adapter.config;

import lombok.Data;

/**
 * 定义 平台 基础设施或应用能力的配置项及装配规则。
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
     * 执行 平台 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param platformType 用于完成本次业务处理的 platformType 参数。
     * @param baseUrl 用于完成本次业务处理的 baseUrl 参数。
     * @param apiKey 用于完成本次业务处理的 apiKey 参数。
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @param temperature 用于完成本次业务处理的 temperature 参数。
     * @param maxTokens 用于完成本次业务处理的 maxTokens 参数。
     * @param maxRetries 用于完成本次业务处理的 maxRetries 参数。
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
     * 执行 平台 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param platformType 用于完成本次业务处理的 platformType 参数。
     * @param adapterType 用于完成本次业务处理的 adapterType 参数。
     * @param baseUrl 用于完成本次业务处理的 baseUrl 参数。
     * @param apiKey 用于完成本次业务处理的 apiKey 参数。
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @param temperature 用于完成本次业务处理的 temperature 参数。
     * @param maxTokens 用于完成本次业务处理的 maxTokens 参数。
     * @param maxRetries 用于完成本次业务处理的 maxRetries 参数。
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
     * 校验或判断 平台 相关业务数据，并返回处理结果。
     *
     * @return 返回本次条件判断是否成立。
     */
    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    /**
     * 校验或判断 平台 相关业务数据，并返回处理结果。
     *
     * @return 返回本次条件判断是否成立。
     */
    public boolean isBaseUrlConfigured() {
        return baseUrl != null && !baseUrl.trim().isEmpty();
    }
}
