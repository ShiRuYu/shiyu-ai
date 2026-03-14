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
 * DeepSeek 模型适配器
 */
@Slf4j
@Component("deepseekModelAdapter")
public class DeepSeekAdapter extends AbstractModelAdapter {

    private static final String DEFAULT_BASE_URL = "https://api.deepseek.com";
    private static final String DEFAULT_MODEL = "deepseek-chat";

    public DeepSeekAdapter(ModelProperties modelProperties) {
        String apiKey = System.getenv("DEEPSEEK_API_KEY");
        
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("DeepSeek API Key 未配置，请设置环境变量 DEEPSEEK_API_KEY");
        } else {
            createChatClient(DEFAULT_MODEL, DEFAULT_BASE_URL, apiKey);
            log.info("DeepSeek 默认 ChatClient 初始化成功，baseUrl: {}, model: {}", DEFAULT_BASE_URL, DEFAULT_MODEL);
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
        return ModelEnum.DEEPSEEK;
    }

    @Override
    protected ChatClient doGetChatClient(String modelName) {
        String apiKey = System.getenv("DEEPSEEK_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            return null;
        }
        
        if (modelName == null || modelName.isEmpty()) {
            return createChatClient(DEFAULT_MODEL, DEFAULT_BASE_URL, apiKey);
        }
        return createChatClient(modelName, DEFAULT_BASE_URL, apiKey);
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
