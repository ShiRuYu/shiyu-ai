package com.shiyu.ai.chat.lm.model;

import com.shiyu.ai.chat.lm.ModelEnum;
import com.shiyu.ai.chat.lm.request.ModelRequest;
import com.shiyu.ai.chat.lm.result.ModelResult;
import org.springframework.ai.chat.client.ChatClient;

public interface ModelAdapter {
    ModelEnum getType();
    ChatClient getChatClient();
    ModelResult call(ModelRequest request);
    ModelResult stream(ModelRequest request);
}
