package com.shiyu.ai.model.contract.api;

import com.shiyu.ai.model.contract.model.ChatRequest;
import com.shiyu.ai.model.contract.model.ChatResponse;

import reactor.core.publisher.Flux;

/**
 * 定义 对话 相关的协作契约和调用边界。
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
