/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shiyu.ai.agent.controller;

import com.shiyu.ai.common.core.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("test")
@Slf4j
public class OpenAiChatModel1Controller implements EnvironmentAware {

    private final ChatModel openAiChatModel;

    private final ChatClient openAiChatClient;

    public OpenAiChatModel1Controller(ChatModel chatModel, SyncMcpToolCallbackProvider provider) {
        this.openAiChatModel = chatModel;
        this.openAiChatClient = ChatClient.builder(chatModel)
                .defaultToolCallbacks(provider.getToolCallbacks())
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    /**
     * 最简单的使用方式，没有任何 LLMs 参数注入。
     *
     * @return String types.
     */
    @GetMapping("chat-model/simple/chat")
    public String simpleChat(String message) {
        return openAiChatModel.call(new Prompt(message)).getResult().getOutput().getText();
    }

    /**
     * Stream 流式调用。可以使大模型的输出信息实现打字机效果。
     *
     * @return Flux<String> types.
     */
    @GetMapping("chat-model/stream/chat")
    public Flux<String> streamChat(String message) {
        Flux<ChatResponse> chatResponseFlux = openAiChatModel.stream(new Prompt(message));
        return chatResponseFlux.mapNotNull(resp -> resp.getResult().getOutput().getText());
    }

    @GetMapping("chat-client/chat")
    public String clientChat(String message) {
        return ObjectUtils.requireNonNull(openAiChatClient.prompt(new Prompt(message)).call().chatResponse()).getResult().getOutput().getText();
    }

    @GetMapping("client/stream/chat")
    public Flux<String> clientStreamChat(String message) {
        return openAiChatClient.prompt(message).stream().content();
    }

    private Environment environment1;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment1 = environment;
    }
}
