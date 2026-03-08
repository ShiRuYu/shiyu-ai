package com.shiyu.ai.chat.domain.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 记忆上下文，包含当前会话相关的记忆和历史信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryContext {
    
    /**
     * 会话 ID
     */
    private String sessionId;
    
    /**
     * 用户 ID
     */
    private String userId;
    
    /**
     * 短期记忆列表（最近的对话历史）
     */
    private List<Memory> shortTermMemories;
    
    /**
     * 长期记忆列表（重要信息）
     */
    private List<Memory> longTermMemories;
    
    /**
     * 最近对话历史
     */
    private List<ConversationHistory> recentHistories;
    
    /**
     * 记忆摘要（对长期记忆的总结）
     */
    private String memorySummary;
}
