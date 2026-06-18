package com.shiyu.ai.agent.biz.agent.config;

import com.shiyu.ai.common.core.factory.YmlPropertySourceFactory;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "shiyu.ai")
@PropertySource(value = "classpath:/config/config.yml", factory = YmlPropertySourceFactory.class)
public class PlatformProperties {

    /**
     * 平台编码 -> API Key 映射表，由 @PostConstruct 从各子配置中汇总
     */
    private final Map<String, String> platformApiKeys = new HashMap<>();

    /**
     * ???? -> ???????
     */
    private final Map<String, String> platformDefaultModels = new HashMap<>();
    private OllamaConfig ollama = new OllamaConfig();
    private DeepSeekConfig deepseek = new DeepSeekConfig();
    private OpenAIConfig openai = new OpenAIConfig();
    private OpenRouterConfig openrouter = new OpenRouterConfig();
    private SiliconFlowConfig siliconflow = new SiliconFlowConfig();

    @Data
    public static class OllamaConfig {
        private String baseUrl = "http://localhost:11434";
        private String apiKey = "";
        private String model = "gemma3:4b";
    }

    @Data
    public static class DeepSeekConfig {
        private String baseUrl = "https://api.deepseek.com";
        private String apiKey = "";
        private String model = "deepseek-chat";
    }

    @Data
    public static class OpenAIConfig {
        private String baseUrl = "https://api.openai.com/v1";
        private String apiKey = "";
        private String model = "gpt-4o-mini";
    }

    @Data
    public static class OpenRouterConfig {
        private String baseUrl = "https://openrouter.ai/api";
        private String apiKey = "";
        private String model = "x-ai/grok-4.1-fast";
    }

    /**
     * ?????????????????? Map ?
     */
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
     * ?????????? API Key
     * ?? Lc4jModelManager ?? switch-case ???
     */
    public String getApiKey(String platformCode) {
        if (platformCode == null) return null;
        return platformApiKeys.get(platformCode.toUpperCase());
    }

    /**
     * ??????????????
     */
    public String getDefaultModel(String platformCode) {
        if (platformCode == null) return null;
        return platformDefaultModels.get(platformCode.toUpperCase());
    }

    @Data
    public static class SiliconFlowConfig {
        private String baseUrl = "https://api.siliconflow.cn";
        private String apiKey = "";
        private String model = "THUDM/GLM-Z1-9B-0414";
    }
}
