package com.shiyu.ai.aiagent.node.intent;

import com.shiyu.ai.aiagent.node.NodeConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Builder;
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
    @Builder.Default
    private Double confidenceThreshold = 0.75;
    
    /**
     * 意图分类（用于从 {@link IntentDefinitionFactory} 中查找对应的意图定义）
     */
    private String category;
    
    /**
     * 是否启用模糊匹配（默认 false）
     */
    @Builder.Default
    private Boolean enableFuzzyMatch = false;
    
    /**
     * 最大重试次数（默认 3）
     */
    @Builder.Default
    private Integer maxRetries = 3;
    
    /**
     * 意图识别使用的 LLM 平台（null 则使用默认平台）
     */
    private String platform;
    
    /**
     * 意图识别使用的模型名称（null 则使用默认模型）
     */
    private String modelName;
}
