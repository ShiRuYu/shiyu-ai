package com.shiyu.ai.chat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 记忆功能配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "shiyu.memory")
public class MemoryConfig {
    
    /**
     * 是否启用记忆功能
     */
    private boolean enabled = true;
    
    /**
     * 短期记忆最大数量（每个会话）
     */
    private int maxShortTermMemories = 10;
    
    /**
     * 长期记忆最大数量（每个用户）
     */
    private int maxLongTermMemories = 50;
    
    /**
     * 对话历史最大保留数量（每个会话）
     */
    private int maxHistoryRecords = 5;
    
    /**
     * 短期记忆过期时间（小时）
     */
    private long shortTermExpireHours = 24;
    
    /**
     * 是否自动提取长期记忆
     */
    private boolean autoExtractLongTerm = true;
    
    /**
     * 长期记忆提取的最小重要性分数（0-1）
     */
    private double longTermMinScore = 0.7;
}
