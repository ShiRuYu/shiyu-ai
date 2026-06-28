package com.shiyu.ai.aiagent.node.memory;

import com.shiyu.ai.aiagent.node.NodeConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Builder;

/**
 * 记忆检索节点配置类
 * 用于从记忆中检索相关信息
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MemoryRetrievalConfig extends NodeConfig {
    
    /**
     * 检索范围（默认：ALL）
     */
    @Builder.Default
    private String retrievalScope = "ALL";
    
    /**
     * 最大检索结果数（默认 10）
     */
    @Builder.Default
    private Integer topK = 10;
    
    /**
     * 相似度阈值（默认 0.6）
     */
    @Builder.Default
    private Double similarityThreshold = 0.6;
    
    /**
     * 是否包含元数据（默认 true）
     */
    @Builder.Default
    private Boolean includeMetadata = true;
}
