package com.shiyu.ai.agent.service;

import com.shiyu.ai.agent.domain.AgentChatRequest;
import com.shiyu.ai.agent.domain.AgentChatResponse;
import reactor.core.publisher.Flux;

/**
 * Agent 对话服务接口
 * 基于 Lc4jModelManager 提供大模型调用能力
 */
public interface AgentChatService {
    
    /**
     * 普通对话（同步调用）
     * @param request 对话请求（包含 platform、model、prompt）
     * @return 对话响应
     */
    AgentChatResponse call(AgentChatRequest request);
    
    /**
     * 流式对话
     * @param request 对话请求（包含 platform、model、prompt）
     * @return 流式响应
     */
    Flux<String> stream(AgentChatRequest request);
}
