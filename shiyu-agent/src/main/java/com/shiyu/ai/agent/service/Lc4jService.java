package com.shiyu.ai.agent.service;

import com.shiyu.ai.agent.domain.Lc4jRequest;
import com.shiyu.ai.agent.domain.Lc4jResponse;
import reactor.core.publisher.Flux;

/**
 * LangChain4j 服务接口
 * 基于 Lc4jModelManager 提供大模型调用能力
 */
public interface Lc4jService {
    
    /**
     * 普通对话（同步调用）
     * @param request 对话请求（包含 platform、model、prompt）
     * @return 对话响应
     */
    Lc4jResponse call(Lc4jRequest request);
    
    /**
     * 流式对话
     * @param request 对话请求（包含 platform、model、prompt）
     * @return 流式响应
     */
    Flux<String> stream(Lc4jRequest request);
}
