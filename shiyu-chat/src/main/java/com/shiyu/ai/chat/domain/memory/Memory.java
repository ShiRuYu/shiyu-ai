package com.shiyu.ai.chat.domain.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 记忆实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Memory {
    
    /**
     * 记忆 ID
     */
    private String id;
    
    /**
     * 会话 ID（用于关联同一会话的多轮对话）
     */
    private String sessionId;
    
    /**
     * 用户 ID
     */
    private String userId;
    
    /**
     * 记忆类型：SHORT_TERM(短期)/LONG_TERM(长期)
     */
    private MemoryType type;
    
    /**
     * 记忆内容
     */
    private String content;
    
    /**
     * 记忆的权重（重要性评分 0-1）
     */
    private Double weight;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 过期时间（短期记忆有过期时间）
     */
    private LocalDateTime expiresAt;
    
    /**
     * 元数据（JSON 格式，存储额外信息）
     */
    private String metadata;
    
    public enum MemoryType {
        SHORT_TERM,  // 短期记忆：存储最近的对话历史
        LONG_TERM    // 长期记忆：存储重要信息和知识点
    }
}
