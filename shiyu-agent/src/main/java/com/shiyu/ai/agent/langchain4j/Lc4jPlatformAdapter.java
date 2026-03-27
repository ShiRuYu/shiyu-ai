package com.shiyu.ai.agent.langchain4j;

import com.shiyu.ai.agent.langchain4j.config.Lc4jPlatformConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * LangChain4j 平台适配器接口
 * 用于适配不同的 AI 平台提供商
 */
public interface Lc4jPlatformAdapter {
    
    /**
     * 获取平台类型标识
     * @return 平台类型（如：OPENROUTER, OLLAMA, DEEPSEEK 等）
     */
    String getPlatformType();
    
    /**
     * 获取同步聊天模型
     * @param modelName 模型名称，为空时使用默认模型
     * @return ChatLanguageModel 实例
     */
    ChatModel getChatModel(String modelName);
    
    /**
     * 获取流式聊天模型
     * @param modelName 模型名称，为空时使用默认模型
     * @return StreamingChatLanguageModel 实例
     */
    StreamingChatModel getStreamingChatModel(String modelName);
    
    /**
     * 获取默认模型名称
     * @return 默认模型名称
     */
    String getDefaultModelName();
    
    /**
     * 检查平台是否可用
     * @return true-可用，false-不可用
     */
    boolean isAvailable();
    
    /**
     * 清空模型缓存
     * 用于配置变更时刷新缓存
     */
    void clearCache();
    
    /**
     * 根据动态配置创建同步模型实例
     * @param config 平台配置
     * @param modelName 模型名称
     * @return ChatModel 实例
     */
    ChatModel createChatModel(Lc4jPlatformConfig config, String modelName);
    
    /**
     * 根据动态配置创建流式模型实例
     * @param config 平台配置
     * @param modelName 模型名称
     * @return StreamingChatModel 实例
     */
    StreamingChatModel createStreamingChatModel(Lc4jPlatformConfig config, String modelName);
}
