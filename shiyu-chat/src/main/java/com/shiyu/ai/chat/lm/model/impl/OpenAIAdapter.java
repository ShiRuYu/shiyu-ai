package com.shiyu.ai.chat.lm.model.impl;

import com.shiyu.ai.chat.lm.model.ModelAdapter;
import com.shiyu.ai.chat.lm.ModelEnum;
import com.shiyu.ai.chat.lm.request.ModelRequest;
import com.shiyu.ai.chat.lm.result.ModelResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component("openAIModelAdapter")
public class OpenAIAdapter implements ModelAdapter {

    @Override
    public ModelEnum getType() {
        return ModelEnum.OPENAI;
    }
    @Override
    public ModelResult call(ModelRequest request) {
        return new ModelResult("OpenAI response for: " + request.getPrompt());
    }

    @Override
    public ModelResult stream(ModelRequest request) {
        return new ModelResult(Flux.just("OpenAI response for: Flux"));
    }
}
