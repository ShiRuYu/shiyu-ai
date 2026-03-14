package com.shiyu.ai.chat.config;

import com.shiyu.ai.common.core.factory.YmlPropertySourceFactory;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@ConfigurationProperties(prefix = "shiyu.ai")
@PropertySource(value = "classpath:/config/config.yml", factory = YmlPropertySourceFactory.class)
public class PlatformProperties {
    private OllamaConfig ollama;
    private DeepSeekConfig deepseek;
    private OpenAIConfig openai;
    private OpenRouterConfig openrouter;
    private SiliconFlowConfig siliconflow;

    @Data
    public static class OllamaConfig {
        private String baseUrl = "http://localhost:11434";
        private String apiKey = "";
        private String model = "x-ai/grok-4.1-fast";
    }

    @Data
    public static class DeepSeekConfig {
        private String baseUrl = "https://api.deepseek.com";
        private String apiKey = "";
        private String model = "x-ai/grok-4.1-fast";
    }

    @Data
    public static class OpenAIConfig {
        private String baseUrl = "https://api.openai.com/v1";
        private String apiKey = "";
        private String model = "x-ai/grok-4.1-fast";
    }

    @Data
    public static class OpenRouterConfig {
        private String baseUrl = "https://openrouter.ai/api";
        private String apiKey = "";
        private String model = "x-ai/grok-4.1-fast";
    }

    @Data
    public static class SiliconFlowConfig {
        private String baseUrl = "https://api.siliconflow.cn";
        private String apiKey = "";
        private String model = "THUDM/GLM-Z1-9B-0414";
    }

}
