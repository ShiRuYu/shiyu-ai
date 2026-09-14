package com.shiyu.ai.model.contract.api;

import com.shiyu.ai.model.contract.model.ChatRequest;
import com.shiyu.ai.model.contract.model.ChatResponse;

import reactor.core.publisher.Flux;

/**
 * ChatEngine 接口，定义模型模块的能力边界。
 */
public interface ChatEngine {
    /**
     * 执行聊天生成。
     *
     * @param request 请求对象。
     *
     * @return 处理结果。
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 执行流式聊天生成。
     *
     * @param request 请求对象。
     *
     * @return 处理结果。
     */
    Flux<ChatResponse> stream(ChatRequest request);
}
