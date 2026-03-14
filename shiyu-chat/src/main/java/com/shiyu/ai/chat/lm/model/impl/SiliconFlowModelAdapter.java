package com.shiyu.ai.chat.lm.model.impl;

import com.shiyu.ai.chat.config.ModelProperties;
import com.shiyu.ai.chat.lm.PlatformEnum;
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 硅基流动模型适配器
 */
@Slf4j
@Component("siliconFlowModelAdapter")
public class SiliconFlowModelAdapter extends AbstractModelAdapter {

    private final ModelProperties.SiliconFlowConfig defaultConfig;
    
    /**
     * ChatClient 缓存（按 modelName 缓存）
     */
    private final Map<String, ChatClient> chatClientCache = new ConcurrentHashMap<>();

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
        ChatClient client = ChatClient.builder(chatModel).build();
        
        // 缓存 ChatClient
        chatClientCache.put(modelName, client);
        return client;
    }
    
    private ChatClient getOrCreateChatClient(String modelName) {
        if (modelName == null || modelName.isEmpty()) {
            modelName = defaultConfig.getModel();
        }
        
        return chatClientCache.computeIfAbsent(modelName, key -> {
            log.debug("Creating ChatClient for model: {}", key);
            return createChatClient(key, defaultConfig.getBaseUrl(), defaultConfig.getApiKey());
        });
    }

    @Override
    protected String doCall(ModelRequest request) {
        String modelName = request.getModelName();
        ChatClient client = getOrCreateChatClient(modelName);
        return client.prompt(request.getPrompt()).call().content();
    }

    @Override
    protected Flux<String> doStream(ModelRequest request) {
        String modelName = request.getModelName();
        ChatClient client = getOrCreateChatClient(modelName);
        return client.prompt(request.getPrompt()).stream().content();
    }
    
    @Override
    public PlatformEnum getType() {
        return PlatformEnum.SILICON_FLOW;
    }
}
