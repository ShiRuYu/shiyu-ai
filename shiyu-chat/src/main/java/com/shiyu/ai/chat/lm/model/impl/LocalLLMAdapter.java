package com.shiyu.ai.chat.lm.model.impl;

import com.shiyu.ai.chat.lm.ModelEnum;
import com.shiyu.ai.chat.lm.model.AbstractModelAdapter;
import com.shiyu.ai.chat.lm.request.ModelRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 本地大模型适配器
 */
@Slf4j
@Component("localModelAdapter")
public class LocalLLMAdapter extends AbstractModelAdapter {

    @Override
    public ModelEnum getType() {
        return ModelEnum.LOCAL;
    }

    @Override
    protected ChatClient doGetChatClient() {
        // TODO: 配置本地运行的模型 ChatClient
        return null;
    }

    @Override
    protected String doCall(ChatClient client, ModelRequest request) {
        // TODO: 实现本地模型的调用逻辑
        return buildMockResponse(request.getPrompt());
    }

    @Override
    protected Flux<String> doStream(ChatClient client, ModelRequest request) {
        // TODO: 实现本地模型的流式调用逻辑
        return buildMockStream(request.getPrompt());
    }
}
