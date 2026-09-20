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
 * 定义 记忆 Retrieval 基础设施或应用能力的配置项及装配规则。
 *
 * @author shiyu-ai
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MemoryRetrievalConfig extends NodeConfig {

    /** 检索范围（默认：ALL） */
    @Builder.Default private String retrievalScope = "ALL";

    /** 最大检索结果数（默认 10） */
    @Builder.Default private Integer topK = 10;

    /** 相似度阈值（默认 0.6） */
    @Builder.Default private Double similarityThreshold = 0.6;

    /** 是否包含元数据（默认 true） */
    @Builder.Default private Boolean includeMetadata = true;
}
