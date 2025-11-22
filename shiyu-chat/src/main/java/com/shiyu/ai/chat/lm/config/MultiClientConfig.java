package com.shiyu.ai.chat.lm.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class MultiClientConfig {

    @Bean
    public ChatModel siliconFlowChatModel(Environment environment) {
        String apiKey = environment.getProperty("spring.ai.siliconflow.api-key");
        String model = environment.getProperty("spring.ai.siliconflow.model");
        assert apiKey != null;
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl("https://api.siliconflow.cn")
                .apiKey(apiKey)
                .build();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(model)
                .build();
        return OpenAiChatModel.builder().openAiApi(api).defaultOptions(options).build();
    }

    @Bean
    public ChatClient siliconFlowChatClient(@Qualifier("siliconFlowChatModel") ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    @Bean
    public ChatModel openRouterChatModel(Environment environment) {
        String apiKey = environment.getProperty("spring.ai.openrouter.api-key");
        String model = environment.getProperty("spring.ai.openrouter.model");
        assert apiKey != null;
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl("https://openrouter.ai/api")
                .apiKey(apiKey)
                .build();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(model)
                .build();
        return OpenAiChatModel.builder().openAiApi(api).defaultOptions(options).build();
    }

    @Bean
    public ChatClient openRouterChatClient(@Qualifier("openRouterChatModel") ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

}
