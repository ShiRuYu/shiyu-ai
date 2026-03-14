package com.shiyu.ai.chat.config;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * 多模型客户端配置
 * 注意：ChatClient 的创建已迁移到各 PlatformAdapter 实现类中
 */
@Configuration
public class MultiClientConfig {

    @Bean
    public EmbeddingModel siliconFlowEmbeddingModel(RestClient.Builder restClientBuilder, ModelProperties modelProperties) {
        ModelProperties.SiliconFlowConfig siliconflow = modelProperties.getSiliconflow();
        String baseUrl = siliconflow.getBaseUrl();
        String apiKey = siliconflow.getApiKey();
        String embedModel = siliconflow.getEmbedModel();
        assert apiKey != null;
        OpenAiApi openAiApi = OpenAiApi.builder()
                .restClientBuilder(restClientBuilder)
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();

        OpenAiEmbeddingOptions openAiEmbeddingOptions = OpenAiEmbeddingOptions.builder()
                .model(embedModel)
                .build();

        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, openAiEmbeddingOptions);
    }

}
