package com.shiyu.ai.chat.service;

import com.shiyu.ai.chat.domain.ChatRequest;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 对话服务接口
 */
public interface ChatService {
    
    /**
     * 普通对话（基于 LiteFlow，支持多轮对话和记忆）
     * @param request 对话请求（包含 query、sessionId、userId、platform、modelName）
     * @return 对话响应（包含 result、intent、chain、sessionId 等）
     */
    Map<String, Object> call(ChatRequest request);
    
    /**
     * 流式对话
     * @param request 对话请求（包含 query、sessionId、userId、platform、modelName）
     * @return 流式响应
     */
    Flux<String> stream(ChatRequest request);
}
