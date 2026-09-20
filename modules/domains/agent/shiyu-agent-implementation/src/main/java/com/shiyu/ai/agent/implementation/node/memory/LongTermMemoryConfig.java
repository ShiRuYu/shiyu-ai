package com.shiyu.ai.agent.implementation.node.memory;

import com.shiyu.ai.agent.contract.node.*;
import com.shiyu.ai.agent.contract.node.NodeConfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 定义 Long Term 记忆 基础设施或应用能力的配置项及装配规则。
 *
 * @author shiyu-ai
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LongTermMemoryConfig extends NodeConfig {

    /** 存储类型（默认：VECTOR_DB） */
    @Builder.Default private String storageType = "VECTOR_DB";

    /** 向量化模型名称 */
    private String embeddingModel;

    /** 最小重要性阈值（默认 0.5） */
    @Builder.Default private Double minImportanceScore = 0.5;

    /** 是否启用压缩（默认 false） */
    @Builder.Default private Boolean enableCompression = false;
}
