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
 * 本地大模型适配器
 */
@Slf4j
@Component("localModelAdapter")
public class LocalLLMAdapter extends AbstractModelAdapter {

    private static final String DEFAULT_BASE_URL = "http://localhost:11434";
    private static final String DEFAULT_API_KEY = "ollama";

    public LocalLLMAdapter(ModelProperties modelProperties) {
        String modelName = System.getenv("LOCAL_MODEL_NAME");
        
        if (modelName == null || modelName.isEmpty()) {
            log.warn("本地模型名称未配置，请设置环境变量 LOCAL_MODEL_NAME，使用 Mock 响应");
        } else {
            createChatClient(modelName, DEFAULT_BASE_URL, DEFAULT_API_KEY);
            log.info("本地模型 ChatClient 初始化成功，baseUrl: {}, model: {}", DEFAULT_BASE_URL, modelName);
        }
    }
    
    private ChatClient createChatClient(String modelName, String baseUrl, String apiKey) {
        OpenAiApi api = OpenAiApi.builder().baseUrl(baseUrl).apiKey(apiKey).build();
        OpenAiChatOptions options = OpenAiChatOptions.builder().model(modelName).build();
        ChatModel chatModel = OpenAiChatModel.builder().openAiApi(api).defaultOptions(options).build();
        return ChatClient.builder(chatModel).build();
    }

    @Override
    public ModelEnum getType() {
        return ModelEnum.LOCAL;
    }

    @Override
    protected ChatClient doGetChatClient(String modelName) {
        String envModelName = System.getenv("LOCAL_MODEL_NAME");
        if (envModelName == null || envModelName.isEmpty()) {
            return null;
        }
        
        if (modelName == null || modelName.isEmpty()) {
            return createChatClient(envModelName, DEFAULT_BASE_URL, DEFAULT_API_KEY);
        }
        return createChatClient(modelName, DEFAULT_BASE_URL, DEFAULT_API_KEY);
    }

    @Override
    protected String doCall(ChatClient client, ModelRequest request) {
        return client.prompt(request.getPrompt()).call().content();
    }

    @Override
    protected Flux<String> doStream(ChatClient client, ModelRequest request) {
        return client.prompt(request.getPrompt()).stream().content();
    }
}
