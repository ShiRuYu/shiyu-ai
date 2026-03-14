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
 * 硅基流动模型适配器
 */
@Slf4j
@Component("siliconFlowModelAdapter")
public class SiliconFlowModelAdapter extends AbstractModelAdapter {

    private final ModelProperties.SiliconFlowConfig defaultConfig;

    public SiliconFlowModelAdapter(ModelProperties modelProperties) {
        this.defaultConfig = modelProperties.getSiliconflow();
        String baseUrl = defaultConfig.getBaseUrl();
        String apiKey = defaultConfig.getApiKey();
        String model = defaultConfig.getModel();
        
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("SiliconFlow API Key 未配置");
        } else {
            createChatClient(model, baseUrl, apiKey);
            log.info("SiliconFlow 默认 ChatClient 初始化成功，baseUrl: {}, model: {}", baseUrl, model);
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
        return ModelEnum.SILICON_FLOW;
    }

    @Override
    protected ChatClient doGetChatClient(String modelName) {
        if (modelName == null || modelName.isEmpty()) {
            return createChatClient(defaultConfig.getModel(), defaultConfig.getBaseUrl(), defaultConfig.getApiKey());
        }
        return createChatClient(modelName, defaultConfig.getBaseUrl(), defaultConfig.getApiKey());
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
