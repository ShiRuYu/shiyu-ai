package com.shiyu.ai.agent.biz.agent.config;

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
