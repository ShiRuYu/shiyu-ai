package com.shiyu.ai.agent.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent 对话请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentChatRequest {
    
    /**
     * 平台类型（如：OPENROUTER, OLLAMA, DEEPSEEK, OPENAI, SILICON_FLOW）
     */
    private String platform;
    
    /**
     * 模型名称，为空时使用平台默认模型
     */
    private String model;
    
    /**
     * 用户输入的问题
     */
    private String prompt;
}
