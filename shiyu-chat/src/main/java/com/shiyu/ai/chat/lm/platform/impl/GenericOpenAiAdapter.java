package com.shiyu.ai.chat.lm.platform.impl;

import com.shiyu.ai.chat.lm.PlatformEnum;
import com.shiyu.ai.chat.lm.platform.AbstractPlatformAdapter;
import com.shiyu.ai.chat.lm.request.LmRequest;
import com.shiyu.ai.chat.lm.result.ChatResult;
import com.shiyu.ai.chat.lm.result.StreamResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class GenericOpenAiAdapter extends AbstractPlatformAdapter {

    private final PlatformEnum platformType;
    private final String baseUrl;
    private final String apiKey;
    private final String defaultModel;
    private final Map<String, ChatClient> chatClientCache = new ConcurrentHashMap<>();

    public GenericOpenAiAdapter(PlatformEnum platformType, String baseUrl, String apiKey, String defaultModel) {
        this.platformType = platformType;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.defaultModel = defaultModel;
        log.info("{} Adapter 初始化成功，baseUrl: {}", platformType, baseUrl);
    }

    private ChatClient createChatClient(String modelName) {
        OpenAIClient client = OpenAIOkHttpClient.builder().baseUrl(baseUrl).apiKey(apiKey).build();
        OpenAiChatOptions options = OpenAiChatOptions.builder().model(modelName).build();
        ChatModel chatModel = OpenAiChatModel.builder().openAiClient(client).options(options).build();
        ChatClient chatClient = ChatClient.builder(chatModel).build();
        chatClientCache.put(modelName, chatClient);
        return chatClient;
    }

    private ChatClient getOrCreateChatClient(String modelName) {
        String name = (modelName != null && !modelName.isEmpty()) ? modelName : defaultModel;
        return chatClientCache.computeIfAbsent(name, k -> {
            log.debug("创建 ChatClient for model: {}", k);
            return createChatClient(k);
        });
    }

    @Override
    protected ChatResult doCall(LmRequest request) {
        ChatClient client = getOrCreateChatClient(request.getModelName());
        return new ChatResult(client.prompt(request.getPrompt()).call().content());
    }

    @Override
    protected StreamResult doStream(LmRequest request) {
        ChatClient client = getOrCreateChatClient(request.getModelName());
        return new StreamResult(client.prompt(request.getPrompt()).stream().content());
    }

    @Override
    public PlatformEnum getType() {
        return platformType;
    }
}