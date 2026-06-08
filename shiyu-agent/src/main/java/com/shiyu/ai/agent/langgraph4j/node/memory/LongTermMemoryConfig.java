package com.shiyu.ai.agent.langgraph4j.node.memory;

import com.shiyu.ai.agent.langgraph4j.node.NodeConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Builder;

/**
 * 长期记忆节点配置类
 * 用于存储和管理重要信息和知识点
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LongTermMemoryConfig extends NodeConfig {
    
    /**
     * 存储类型（默认：VECTOR_DB）
     */
    @Builder.Default
    private String storageType = "VECTOR_DB";
    
    /**
     * 向量化模型名称
     */
    private String embeddingModel;
    
    /**
     * 最小重要性阈值（默认 0.5）
     */
    @Builder.Default
    private Double minImportanceScore = 0.5;
    
    /**
     * 是否启用压缩（默认 false）
     */
    @Builder.Default
    private Boolean enableCompression = false;
}
