package com.shiyu.ai.agent.langgraph4j.node.intent;

import com.shiyu.ai.agent.langgraph4j.node.NodeConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 意图节点配置类
 * 用于配置意图识别节点的各项参数
 *
 * @author shiyu-ai
 * @date 2026-03-26
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IntentConfig extends NodeConfig {
    
    /**
     * 意图类型（使用枚举）
     */
    private IntentType intentType;
    
    /**
     * 意图类型代码（字符串形式，便于配置）
     */
    private String intentTypeCode;
    
    /**
     * 置信度阈值（默认 0.75）
     */
    private Double confidenceThreshold = 0.75;
    
    /**
     * 意图分类（用于从 {@link IntentDefinitionFactory} 中查找对应的意图定义）
     */
    private String category;
    
    /**
     * 是否启用模糊匹配（默认 false）
     */
    private Boolean enableFuzzyMatch = false;
    
    /**
     * 最大重试次数（默认 3）
     */
    private Integer maxRetries = 3;
}
