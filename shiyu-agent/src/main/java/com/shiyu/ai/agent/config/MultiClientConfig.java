package com.shiyu.ai.agent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MultiClientConfig {

    @Bean
    public ChatModel siliconFlowChatModel(ModelProperties modelProperties) {
        ModelProperties.SiliconFlowConfig siliconflow = modelProperties.getSiliconflow();
        String baseUrl = siliconflow.getBaseUrl();
        String apiKey = siliconflow.getApiKey();
        String model = siliconflow.getModel();
        assert apiKey != null;
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(model)
                .build();
        return OpenAiChatModel.builder().openAiApi(api).defaultOptions(options).build();
    }

    @Bean
    public EmbeddingModel siliconFlowEmbeddingModel(ModelProperties modelProperties) {
        ModelProperties.SiliconFlowConfig siliconflow = modelProperties.getSiliconflow();
        String baseUrl = siliconflow.getBaseUrl();
        String apiKey = siliconflow.getApiKey();
        String embedModel = siliconflow.getEmbedModel();
        assert apiKey != null;
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();

        OpenAiEmbeddingOptions openAiEmbeddingOptions = OpenAiEmbeddingOptions.builder()
                .model(embedModel)
                .build();

        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, openAiEmbeddingOptions);
    }


    @Bean
    public ChatClient siliconFlowChatClient(@Qualifier("siliconFlowChatModel") ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    @Bean
    public ChatModel openRouterChatModel(ModelProperties modelProperties) {
        ModelProperties.SiliconFlowConfig openRouter = modelProperties.getSiliconflow();
        String baseUrl = openRouter.getBaseUrl();
        String apiKey = openRouter.getApiKey();
        String model = openRouter.getModel();
        assert apiKey != null;
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(baseUrl)
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
