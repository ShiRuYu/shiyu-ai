package com.shiyu.ai.chat.lm.model.impl;

import com.shiyu.ai.chat.lm.model.ModelAdapter;
import com.shiyu.ai.chat.lm.ModelEnum;
import com.shiyu.ai.chat.lm.request.ModelRequest;
import com.shiyu.ai.chat.lm.result.ModelResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component("deepseekModelAdapter")
public class DeepSeekAdapter implements ModelAdapter {

    @Override
    public ModelEnum getType() {
        return ModelEnum.DEEPSEEK;
    }

    @Override
    public ChatClient getChatClient() {
        return null;
    }

    @Override
    public ModelResult call(ModelRequest request) {
        return new ModelResult("DeepSeek response for: " + request.getPrompt());
    }

    @Override
    public ModelResult stream(ModelRequest request) {
        return new ModelResult(Flux.just("DeepSeek response for: Flux"));
    }
}

