package com.shiyu.ai.chat.lm.model.impl;

import com.shiyu.ai.chat.lm.model.ModelAdapter;
import com.shiyu.ai.chat.lm.ModelEnum;
import com.shiyu.ai.chat.lm.request.ModelRequest;
import com.shiyu.ai.chat.lm.result.ModelResult;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component("siliconFlowModelAdapter")
public class SiliconFlowModelAdapter implements ModelAdapter {

    @Resource(name = "siliconFlowChatClient")
    private ChatClient chatClient;

    @Override
    public ModelEnum getType() {
        return ModelEnum.SILICON_FLOW;
    }

    @Override
    public ChatClient getChatClient() {
        return this.chatClient;
    }

    @Override
    public ModelResult call(ModelRequest request) {
        String content = chatClient.prompt(request.getPrompt()).call().content();
        return new ModelResult("SiliconFlow response for: " + content);
    }

    @Override
    public ModelResult stream(ModelRequest request) {
        Flux<String> content = chatClient.prompt(request.getPrompt()).stream().content();
        Flux<String> just = Flux.just("SiliconFlow response for: Flux");
        Flux<String> concat = Flux.concat(content, just);
        return new ModelResult(concat);
    }
}

