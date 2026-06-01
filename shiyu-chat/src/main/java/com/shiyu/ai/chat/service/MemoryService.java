package com.shiyu.ai.chat.service;

import com.shiyu.ai.chat.domain.memory.Memory;
import com.shiyu.ai.chat.domain.memory.ConversationHistory;
import com.shiyu.ai.chat.domain.memory.MemoryContext;

import java.util.List;

/**
 * 记忆服务接口
 */
public interface MemoryService {
    
    /**
     * 添加短期记忆
     */
    void addShortTermMemory(Memory memory);
    
    /**
     * 添加长期记忆
     */
    void addLongTermMemory(Memory memory);
    
    /**
     * 获取会话的短期记忆
     */
    List<Memory> getShortTermMemories(String sessionId, int limit);
    
    /**
     * 获取用户的长期记忆
     */
    List<Memory> getLongTermMemories(String userId, int limit);
    
    /**
     * 保存对话历史
     */
    void saveConversationHistory(ConversationHistory history);
    
    /**
     * 获取最近的对话历史
     */
    List<ConversationHistory> getRecentHistories(String sessionId, int limit);
    
    /**
     * 获取完整的记忆上下文（包含短期记忆、长期记忆和历史）
     */
    MemoryContext getMemoryContext(String sessionId, String userId);
    
    /**
     * 清理过期的短期记忆
     */
    void cleanupExpiredMemories();
    
    /**
     * 从记忆中提取关键信息并生成长期记忆（使用默认平台）
     */
    void extractAndStoreLongTermMemory(String sessionId, String query, String response);

    /**
     * 从记忆中提取关键信息并生成长期记忆（指定平台）
     * @param platform 调用 LLM 的平台名称
     */
    void extractAndStoreLongTermMemory(String sessionId, String query, String response, String platform);
    
    /**
     * 保存对话记忆（对话历史 + 短期记忆 + 长期记忆）
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param query 用户问题
     * @param response AI回复
     * @param intentType 意图类型
     * @param chainUsed 使用的链
     */
    void saveChatMemory(String sessionId, String userId, String query, String response,
                        String intentType, String chainUsed);

    /**
     * 构建带记忆的提示词（将记忆信息整合到 Prompt 中）
     */
    String buildPromptWithMemory(String originalPrompt, MemoryContext memoryContext);
}
