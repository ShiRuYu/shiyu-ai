package com.shiyu.ai.chat.lm.platform.impl;

import com.shiyu.ai.chat.config.PlateformProperties;
import com.shiyu.ai.chat.lm.PlatformEnum;
import com.shiyu.ai.chat.lm.platform.AbstractPlatformAdapter;
import com.shiyu.ai.chat.lm.request.LmRequest;
import com.shiyu.ai.chat.lm.result.ChatResult;
import com.shiyu.ai.chat.lm.result.StreamResult;
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
 * OpenRouter 模型适配器
 */
@Slf4j
@Component("openRouterModelAdapter")
public class OpenRouterModelAdapter extends AbstractPlatformAdapter {

    private final PlateformProperties.OpenRouterConfig defaultConfig;
    
    /**
     * ChatClient 缓存（按 modelName 缓存）
     */
    private final Map<String, ChatClient> chatClientCache = new ConcurrentHashMap<>();

    public OpenRouterModelAdapter(PlateformProperties modelProperties) {
        this.defaultConfig = modelProperties.getOpenrouter();
        String baseUrl = defaultConfig.getBaseUrl();
        String apiKey = defaultConfig.getApiKey();
        String model = defaultConfig.getModel();
        
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("OpenRouter API Key 未配置");
        } else {
            createChatClient(model, baseUrl, apiKey);
            log.info("OpenRouter 默认 ChatClient 初始化成功，baseUrl: {}, model: {}", baseUrl, model);
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
    protected ChatResult doCall(LmRequest request) {
        String modelName = request.getModelName();
        ChatClient client = getOrCreateChatClient(modelName);
        String response = client.prompt(request.getPrompt()).call().content();
        return new ChatResult(response);
    }

    @Override
    protected StreamResult doStream(LmRequest request) {
        String modelName = request.getModelName();
        ChatClient client = getOrCreateChatClient(modelName);
        Flux<String> response = client.prompt(request.getPrompt()).stream().content();
        return new StreamResult(response);
    }
    
    @Override
    public PlatformEnum getType() {
        return PlatformEnum.OPEN_ROUTER;
    }
}
