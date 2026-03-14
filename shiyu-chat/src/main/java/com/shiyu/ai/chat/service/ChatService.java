package com.shiyu.ai.chat.service;

import com.shiyu.ai.chat.domain.ChatRequest;

import java.util.Map;

/**
 * 对话服务接口
 */
public interface ChatService {
    
    /**
     * 普通对话（基于 LiteFlow，支持多轮对话和记忆）
     * @param request 对话请求
     * @return 对话响应（包含 result、intent、chain、sessionId 等）
     */
    Map<String, Object> chat(ChatRequest request);
    
    /**
     * 流式对话
     * @param text 用户输入文本
     * @param platformEnum 平台枚举（如：SILICON_FLOW、OPENAI 等）
     * @return 流式响应
     */
    reactor.core.publisher.Flux<String> stream(String text, String platformEnum);
}
