package com.shiyu.ai.agent.node.intent;

import lombok.Data;

/**
 * 意图节点配置类
 * 用于配置意图识别节点的各项参数
 *
 * @author shiyu-ai
 * @date 2026-03-26
 */
@Data
public class IntentConfig {
    
    /**
     * 意图类型
     */
    private String intentType;
    
    /**
     * 置信度阈值（默认 0.75）
     */
    private Double confidenceThreshold = 0.75;
    
    /**
     * 支持的意图列表
     */
    private String[] supportedIntents;
    
    /**
     * 是否启用模糊匹配（默认 false）
     */
    private Boolean enableFuzzyMatch = false;
    
    /**
     * 最大重试次数（默认 3）
     */
    private Integer maxRetries = 3;
}
