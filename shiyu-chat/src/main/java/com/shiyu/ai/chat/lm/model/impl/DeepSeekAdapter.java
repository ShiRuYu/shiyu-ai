package com.shiyu.ai.chat.lm.model.impl;

import com.shiyu.ai.chat.lm.model.ModelAdapter;
import com.shiyu.ai.chat.lm.model.ModelConfig;
import com.shiyu.ai.chat.lm.request.ModelRequest;
import com.shiyu.ai.chat.lm.result.ModelResult;
import org.springframework.stereotype.Component;

@Component("deepseekModelAdapter")
public class DeepSeekAdapter implements ModelAdapter {

    @Override
    public ModelResult call(ModelConfig config, ModelRequest request) {
        return new ModelResult("DeepSeek response for: " + request.getPrompt());
    }
}

