package com.shiyu.ai.agent.node.memory;

import com.shiyu.ai.agent.node.NodeConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 长期记忆节点配置类
 * 用于存储和管理重要信息和知识点
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LongTermMemoryConfig extends NodeConfig {
    
    /**
     * 存储类型（默认：VECTOR_DB）
     */
    private String storageType = "VECTOR_DB";
    
    /**
     * 向量化模型名称
     */
    private String embeddingModel;
    
    /**
     * 最小重要性阈值（默认 0.5）
     */
    private Double minImportanceScore = 0.5;
    
    /**
     * 是否启用压缩（默认 false）
     */
    private Boolean enableCompression = false;
}
