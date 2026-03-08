package com.shiyu.ai.chat.domain.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 对话历史记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationHistory {
    
    /**
     * 记录 ID
     */
    private String id;
    
    /**
     * 会话 ID
     */
    private String sessionId;
    
    /**
     * 用户 ID
     */
    private String userId;
    
    /**
     * 用户输入
     */
    private String userQuery;
    
    /**
     * AI 回复
     */
    private String aiResponse;
    
    /**
     * 使用的意图类型
     */
    private String intentType;
    
    /**
     * 使用的对话模式（Direct/CoT/ToT）
     */
    private String chainUsed;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 元数据（存储完整的上下文信息）
     */
    private String metadata;
}
