package com.shiyu.ai.chat.lm.model.impl;

import com.shiyu.ai.chat.lm.ModelEnum;
import com.shiyu.ai.chat.lm.model.AbstractModelAdapter;
import com.shiyu.ai.chat.lm.request.ModelRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * DeepSeek 模型适配器
 */
@Slf4j
@Component("deepseekModelAdapter")
public class DeepSeekAdapter extends AbstractModelAdapter {

    @Override
    public ModelEnum getType() {
        return ModelEnum.DEEPSEEK;
    }

    @Override
    protected ChatClient doGetChatClient() {
        // TODO: 配置实际的 ChatClient
        return null;
    }

    @Override
    protected String doCall(ChatClient client, ModelRequest request) {
        // TODO: 实现实际的调用逻辑
        return buildMockResponse(request.getPrompt());
    }

    @Override
    protected Flux<String> doStream(ChatClient client, ModelRequest request) {
        // TODO: 实现实际的流式调用逻辑
        return buildMockStream(request.getPrompt());
    }
}

