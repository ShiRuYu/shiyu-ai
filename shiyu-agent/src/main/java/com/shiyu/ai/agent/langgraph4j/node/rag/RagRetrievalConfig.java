package com.shiyu.ai.agent.langgraph4j.node.rag;

import com.shiyu.ai.agent.langgraph4j.node.NodeConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * RAG 检索节点配置类
 * 用于配置 RAG 检索节点的各项参数
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RagRetrievalConfig extends NodeConfig {
    
    /**
     * 检索知识库 ID
     */
    private String knowledgeBaseId;
    
    /**
     * 检索策略（默认：VECTOR）
     */
    private String retrievalStrategy = "VECTOR";
    
    /**
     * 最大检索结果数（默认 5）
     */
    private Integer topK = 5;
    
    /**
     * 相似度阈值（默认 0.7）
     */
    private Double similarityThreshold = 0.7;
    
    /**
     * 是否启用重排序（默认 false）
     */
    private Boolean enableRerank = false;
}
