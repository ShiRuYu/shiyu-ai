package com.shiyu.ai.core;

import com.shiyu.ai.core.Lc4jRequest;
import com.shiyu.ai.core.Lc4jResponse;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
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
    
    /**
     * 获取 ChatModel 实例
     * @param platformType 平台类型（如：OPENROUTER, OLLAMA 等）
     * @param modelName 模型名称，为空时使用默认模型
     * @return ChatModel 实例
     */
    ChatModel getChatModel(String platformType, String modelName);
    
    /**
     * 获取 StreamingChatModel 实例
     * @param platformType 平台类型（如：OPENROUTER, OLLAMA 等）
     * @param modelName 模型名称，为空时使用默认模型
     * @return StreamingChatModel 实例
     */
    StreamingChatModel getStreamingChatModel(String platformType, String modelName);

    /**
     * 获取默认平台编码
     * @return 默认平台编码（如 OPENAI、DEEPSEEK）
     */
    String getDefaultPlatform();

    /**
     * 获取指定平台的默认模型名称
     * @param platformType 平台编码
     * @return 默认模型名称，如无则返回 null
     */
    String getDefaultModelName(String platformType);
}
