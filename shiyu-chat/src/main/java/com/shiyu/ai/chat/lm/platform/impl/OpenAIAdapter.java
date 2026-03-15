package com.shiyu.ai.chat.lm.platform.impl;

import com.shiyu.ai.chat.config.PlatformProperties;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OpenAI 模型适配器
 */
@Slf4j
@Component("openAIModelAdapter")
public class OpenAIAdapter extends AbstractPlatformAdapter {

    private final PlatformProperties.OpenAIConfig defaultConfig;

    /**
     * ChatClient 缓存（按 modelName 缓存）
     */
    private final Map<String, ChatClient> chatClientCache = new ConcurrentHashMap<>();

    public OpenAIAdapter(ObjectProvider<RestClient.Builder> restClientBuilderProvider,
                         ObjectProvider<WebClient.Builder> webClientBuilderProvider,
                         PlatformProperties platformProperties) {
        this.defaultConfig = platformProperties.getOpenai();
        String baseUrl = defaultConfig.getBaseUrl();
        String apiKey = defaultConfig.getApiKey();
        String model = defaultConfig.getModel();
        this.restClientBuilder = restClientBuilderProvider.getIfAvailable();
        this.webClientBuilder = webClientBuilderProvider.getIfAvailable();

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("OpenAI API Key 未配置");
        } else {
            // 预创建默认模型的 ChatClient
            createChatClient(model, baseUrl, apiKey);
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

        // 使用 builder 模式创建 ChatClient，确保支持流式调用
        ChatClient client = ChatClient.builder(chatModel)
                .build();

        // 缓存 ChatClient
        chatClientCache.put(modelName, client);
        return client;
    }

    /**
     * 获取或创建 ChatClient
     */
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
        String response = client.prompt(request.getPrompt())
                .call()
                .content();
        return new ChatResult(response);
    }

    @Override
    protected StreamResult doStream(LmRequest request) {
        String modelName = request.getModelName();
        ChatClient client = getOrCreateChatClient(modelName);
        Flux<String> response = client.prompt(request.getPrompt())
                .stream()
                .content();
        return new StreamResult(response);
    }

    @Override
    public PlatformEnum getType() {
        return PlatformEnum.OPENAI;
    }
}
