package com.shiyu.ai.chat.lm.model;

import com.shiyu.ai.chat.lm.ModelEnum;
import com.shiyu.ai.chat.lm.request.ModelRequest;
import com.shiyu.ai.chat.lm.result.ModelResult;

public interface ModelAdapter {
    ModelEnum getType();
    ModelResult call(ModelRequest request);
    ModelResult stream(ModelRequest request);
}
