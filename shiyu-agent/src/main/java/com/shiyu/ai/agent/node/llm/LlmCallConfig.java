package com.shiyu.ai.agent.node.llm;

import com.shiyu.ai.agent.node.NodeConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * LLM 调用节点配置类
 * 用于调用大语言模型生成回复
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LlmCallConfig extends NodeConfig {
    
    /**
     * 模型名称
     */
    private String modelName;
    
    /**
     * 温度参数（默认 0.7）
     */
    private Double temperature = 0.7;
    
    /**
     * 最大生成长度（默认 2048）
     */
    private Integer maxTokens = 2048;
    
    /**
     * Top P 参数（默认 0.9）
     */
    private Double topP = 0.9;
    
    /**
     * 系统提示词
     */
    private String systemPrompt;
}
