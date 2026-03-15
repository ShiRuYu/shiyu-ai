package com.shiyu.ai.chat.lm.platform.impl;

import com.shiyu.ai.chat.config.PlatformProperties;
import com.shiyu.ai.chat.lm.PlatformEnum;
import com.shiyu.ai.chat.lm.platform.AbstractPlatformAdapter;
import com.shiyu.ai.chat.lm.request.LmRequest;
import com.shiyu.ai.chat.lm.result.ChatResult;
import com.shiyu.ai.chat.lm.result.StreamResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地大模型适配器
 */
@Slf4j
@Component("ollamaAdapter")
public class OllamaAdapter extends AbstractPlatformAdapter {

    private final PlatformProperties.OllamaConfig defaultConfig;
    
    /**
     * ChatClient 缓存（按 modelName 缓存）
     */
    private final Map<String, ChatClient> chatClientCache = new ConcurrentHashMap<>();

    public OllamaAdapter(ObjectProvider<RestClient.Builder> restClientBuilderProvider,
                         ObjectProvider<WebClient.Builder> webClientBuilderProvider,
                         PlatformProperties platformProperties) {
        this.defaultConfig = platformProperties.getOllama();
        String baseUrl = defaultConfig.getBaseUrl();
        String apiKey = defaultConfig.getApiKey();
        String model = defaultConfig.getModel();
        this.restClientBuilder = restClientBuilderProvider.getIfAvailable();
        this.webClientBuilder = webClientBuilderProvider.getIfAvailable();

        if (model == null || model.isEmpty()) {
            log.warn("本地模型名称未配置，请设置环境变量 LOCAL_MODEL_NAME，使用 Mock 响应");
        } else {
            createChatClient(model, baseUrl, apiKey);
            log.info("本地模型 ChatClient 初始化成功，baseUrl: {}, model: {}", baseUrl, model);
        }
    }
    
    private ChatClient createChatClient(String modelName, String baseUrl, String apiKey) {
        OllamaApi api = OllamaApi.builder().baseUrl(baseUrl).build();
        OllamaChatOptions options = OllamaChatOptions.builder().model(modelName).build();
        OllamaChatModel chatModel = OllamaChatModel.builder().ollamaApi(api).defaultOptions(options).build();

        // 使用 builder 模式创建 ChatClient，确保支持流式调用
        ChatClient client = ChatClient.builder(chatModel)
                .build();
        
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
        if (client == null) {
            throw new IllegalStateException("本地模型未配置");
        }
        String response = client.prompt(request.getPrompt()).call().content();
        return new ChatResult(response);
    }

    @Override
    protected StreamResult doStream(LmRequest request) {
        String modelName = request.getModelName();
        ChatClient client = getOrCreateChatClient(modelName);
        if (client == null) {
            throw new IllegalStateException("本地模型未配置");
        }
        Flux<String> response = client.prompt(request.getPrompt()).stream().content();
        return new StreamResult(response);
    }
    
    @Override
    public PlatformEnum getType() {
        return PlatformEnum.OLLAMA;
    }
}
