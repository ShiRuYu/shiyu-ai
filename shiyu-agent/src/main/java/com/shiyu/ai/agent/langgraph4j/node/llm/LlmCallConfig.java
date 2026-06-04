package com.shiyu.ai.agent.langgraph4j.node.llm;

import com.shiyu.ai.agent.langgraph4j.node.NodeConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * LLM 调用节点配置类
 * 用于调用大语言模型生成回复
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
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
     * 最大生成长度（默认 4096，与适配器默认值保持一致）
     */
    private Integer maxTokens = 4096;
    
    /**
     * Top P 参数（默认 0.9）
     */
    private Double topP = 0.9;
    
    /**
     * 系统提示词
     */
    private String systemPrompt;
    
    /**
     * Prompt 模板
     */
    private String promptTemplate;
    
    /**
     * 默认 Prompt
     */
    private String defaultPrompt;
    
    /**
     * 平台类型
     */
    private String platform;
    
    /**
     * 是否流式调用（默认 false）
     */
    private boolean stream = false;
}
