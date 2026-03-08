package com.shiyu.ai.chat.lm.model.impl;

import com.shiyu.ai.chat.lm.ModelEnum;
import com.shiyu.ai.chat.lm.model.AbstractModelAdapter;
import com.shiyu.ai.chat.lm.request.ModelRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 硅基流动模型适配器
 */
@Slf4j
@Component("siliconFlowModelAdapter")
public class SiliconFlowModelAdapter extends AbstractModelAdapter {

    @Resource(name = "siliconFlowChatClient")
    private ChatClient chatClient;

    @Override
    public ModelEnum getType() {
        return ModelEnum.SILICON_FLOW;
    }

    @Override
    protected ChatClient doGetChatClient() {
        return this.chatClient;
    }

    @Override
    protected String doCall(ChatClient client, ModelRequest request) {
        return client.prompt(request.getPrompt())
                .call()
                .content();
    }

    @Override
    protected Flux<String> doStream(ChatClient client, ModelRequest request) {
        return client.prompt(request.getPrompt())
                .stream()
                .content();
    }
}

