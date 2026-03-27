package com.shiyu.ai.agent.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent 对话响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentChatResponse {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * AI 回复内容
     */
    private String content;
    
    /**
     * 使用的平台
     */
    private String platform;
    
    /**
     * 使用的模型
     */
    private String model;
    
    /**
     * 错误信息（失败时）
     */
    private String errorMessage;
}
