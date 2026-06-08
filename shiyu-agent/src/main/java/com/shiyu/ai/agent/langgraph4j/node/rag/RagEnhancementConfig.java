package com.shiyu.ai.agent.langgraph4j.node.rag;

import com.shiyu.ai.agent.langgraph4j.node.NodeConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Builder;

/**
 * RAG 增强节点配置
 * <p>
 * 配置增强策略、上下文窗口、过滤阈值等参数。
 * 支持策略:
 * <ul>
 *   <li>SUMMARIZATION — 摘要合并（默认）</li>
 *   <li>RE_RANK — 按分数重排序</li>
 *   <li>FILTER — 按相似度阈值过滤</li>
 * </ul>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RagEnhancementConfig extends NodeConfig {
    
    /**
     * 增强策略（默认：SUMMARIZATION）
     */
    @Builder.Default
    private String enhancementStrategy = "SUMMARIZATION";
    
    /**
     * 是否添加上下文（默认 true）
     */
    @Builder.Default
    private Boolean addContext = true;
    
    /**
     * 上下文窗口大小（默认 3，用于 RE_RANK 和 SUMMARIZATION）
     */
    @Builder.Default
    private Integer contextWindowSize = 3;
    
    /**
     * 最大文本长度（默认 2000）
     */
    @Builder.Default
    private Integer maxLength = 2000;
    
    /**
     * 相似度阈值（默认 0.5，用于 FILTER 策略）
     */
    @Builder.Default
    private Double similarityThreshold = 0.5;
}
