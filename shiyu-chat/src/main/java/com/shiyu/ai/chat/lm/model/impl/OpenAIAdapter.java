package com.shiyu.ai.chat.lm.model.impl;

import com.shiyu.ai.chat.config.ModelProperties;
import com.shiyu.ai.chat.lm.ModelEnum;
import com.shiyu.ai.chat.lm.model.AbstractModelAdapter;
import com.shiyu.ai.chat.lm.request.ModelRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * OpenAI 模型适配器
 */
@Slf4j
@Component("openAIModelAdapter")
public class OpenAIAdapter extends AbstractModelAdapter {

    private final ModelProperties.OpenAIConfig defaultConfig;

    public OpenAIAdapter(ModelProperties modelProperties) {
        this.defaultConfig = modelProperties.getOpenai();
        String baseUrl = defaultConfig.getBaseUrl();
        String apiKey = defaultConfig.getApiKey();
        
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("OpenAI API Key 未配置");
        } else {
            // 预创建默认模型的 ChatClient
            createChatClient("gpt-3.5-turbo", baseUrl, apiKey);
            log.info("OpenAI 默认 ChatClient 初始化成功，baseUrl: {}", baseUrl);
        }
    }
    
    /**
     * 创建 ChatClient 实例
     */
    private ChatClient createChatClient(String modelName, String baseUrl, String apiKey) {
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(modelName)
                .build();
        ChatModel chatModel = OpenAiChatModel.builder().openAiApi(api).defaultOptions(options).build();
        return ChatClient.builder(chatModel).build();
    }

    @Override
    public ModelEnum getType() {
        return ModelEnum.OPENAI;
    }

    @Override
    protected ChatClient doGetChatClient(String modelName) {
        if (modelName == null || modelName.isEmpty()) {
            // 使用默认配置
            return createChatClient("gpt-3.5-turbo", defaultConfig.getBaseUrl(), defaultConfig.getApiKey());
        }
        
        // 根据指定的模型名称创建 ChatClient
        return createChatClient(modelName, defaultConfig.getBaseUrl(), defaultConfig.getApiKey());
    }

    @Override
    protected String doCall(ChatClient client, ModelRequest request) {
        return client.prompt(request.getPrompt())
                .call()
                .content();
    }

    @Override
    protected Flux<String> doStream(ChatClient client, ModelRequest request) {
        return client.prompt(request.getPrompt())
                .stream()
                .content();
    }
}
