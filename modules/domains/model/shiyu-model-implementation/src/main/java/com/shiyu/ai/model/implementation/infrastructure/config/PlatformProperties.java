package com.shiyu.ai.model.implementation.infrastructure.config;

import com.shiyu.ai.common.foundation.factory.YmlPropertySourceFactory;

import jakarta.annotation.PostConstruct;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * 定义 平台 基础设施或应用能力的配置项及装配规则。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "shiyu.ai")
@PropertySource(
        value = "classpath:/config/config.yml",
        factory = YmlPropertySourceFactory.class,
        ignoreResourceNotFound = true)
public class PlatformProperties {

    /** 平台编码 -> API Key 映射表，由 @PostConstruct 从各子配置中汇总 */
    private final Map<String, String> platformApiKeys = new HashMap<>();

    /** ???? -> ??????? */
    private final Map<String, String> platformDefaultModels = new HashMap<>();

    /** 租户标识。 */
    private Long tenantId;

    private OllamaConfig ollama = new OllamaConfig();
    private DeepSeekConfig deepseek = new DeepSeekConfig();
    private OpenAIConfig openai = new OpenAIConfig();
    private OpenRouterConfig openrouter = new OpenRouterConfig();
    private SiliconFlowConfig siliconflow = new SiliconFlowConfig();

    /**
     * 定义 Ollama 基础设施或应用能力的配置项及装配规则。
     */
    @Data
    public static class OllamaConfig {
        /**
         * baseUrl 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String baseUrl = "http://localhost:11434";
        /**
         * apiKey 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String apiKey = "";
        /**
         * model 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String model = "gemma3:4b";
    }

    /**
     * 定义 Deep Seek 基础设施或应用能力的配置项及装配规则。
     */
    @Data
    public static class DeepSeekConfig {
        private String baseUrl = "https://api.deepseek.com";
        /**
         * apiKey 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String apiKey = "";
        /**
         * model 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String model = "deepseek-v4-flash";
    }

    /**
     * 定义 Open AI 基础设施或应用能力的配置项及装配规则。
     */
    @Data
    public static class OpenAIConfig {
        private String baseUrl = "https://api.openai.com/v1";
        /**
         * apiKey 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String apiKey = "";
        /**
         * model 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String model = "gpt-4o-mini";
    }

    /**
     * 定义 Open Router 基础设施或应用能力的配置项及装配规则。
     */
    @Data
    public static class OpenRouterConfig {
        private String baseUrl = "https://openrouter.ai/api";
        /**
         * apiKey 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String apiKey = "";
        /**
         * model 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String model = "x-ai/grok-4.1-fast";
    }

    /** ?????????????????? Map ? */
    @PostConstruct
    public void init() {
        platformApiKeys.put("OPENAI", openai.getApiKey());
        platformApiKeys.put("DEEPSEEK", deepseek.getApiKey());
        platformApiKeys.put("OPENROUTER", openrouter.getApiKey());
        platformApiKeys.put("SILICON_FLOW", siliconflow.getApiKey());
        platformApiKeys.put("OLLAMA", ollama.getApiKey());

        platformDefaultModels.put("OPENAI", openai.getModel());
        platformDefaultModels.put("DEEPSEEK", deepseek.getModel());
        platformDefaultModels.put("OPENROUTER", openrouter.getModel());
        platformDefaultModels.put("SILICON_FLOW", siliconflow.getModel());
        platformDefaultModels.put("OLLAMA", ollama.getModel());
    }

    /**
     * 获取apikey。
     *
     * @param platformCode platformCode 参数。
     *
     * @return 处理结果。
     */
    public String getApiKey(String platformCode) {
        if (platformCode == null) return null;
        return platformApiKeys.get(platformCode.toUpperCase());
    }

    /** ?????????????? */
    public String getDefaultModel(String platformCode) {
        if (platformCode == null) return null;
        return platformDefaultModels.get(platformCode.toUpperCase());
    }

    /**
     * 定义 Silicon Flow 基础设施或应用能力的配置项及装配规则。
     */
    @Data
    public static class SiliconFlowConfig {
        private String baseUrl = "https://api.siliconflow.cn";
        /**
         * apiKey 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String apiKey = "";
        /**
         * model 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String model = "THUDM/GLM-Z1-9B-0414";
    }
}
