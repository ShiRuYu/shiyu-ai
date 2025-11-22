package com.shiyu.ai.chat.lm.model.impl;

import com.shiyu.ai.chat.lm.model.ModelAdapter;
import com.shiyu.ai.chat.lm.ModelEnum;
import com.shiyu.ai.chat.lm.request.ModelRequest;
import com.shiyu.ai.chat.lm.result.ModelResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component("localModelAdapter")
public class LocalLLMAdapter implements ModelAdapter {

    @Override
    public ModelEnum getType() {
        return ModelEnum.LOCAL;
    }
    @Override
    public ModelResult call(ModelRequest request) {
        return new ModelResult("Local LLM response for: " + request.getPrompt());
    }

    @Override
    public ModelResult stream(ModelRequest request) {
        return new ModelResult(Flux.just("Local LLM response for: Flux"));
    }
}
