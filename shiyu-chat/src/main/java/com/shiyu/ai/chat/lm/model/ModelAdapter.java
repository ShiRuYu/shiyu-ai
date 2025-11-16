package com.shiyu.ai.chat.lm.model;

import com.shiyu.ai.chat.lm.request.ModelRequest;
import com.shiyu.ai.chat.lm.result.ModelResult;

public interface ModelAdapter {
    ModelResult call(ModelConfig config, ModelRequest request);
}
