package com.shiyu.ai.agent.node.rag;

import com.shiyu.ai.agent.node.NodeConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * RAG 增强节点配置类
 * 用于对检索结果进行增强处理
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RagEnhancementConfig extends NodeConfig {
    
    /**
     * 增强策略（默认：SUMMARIZATION）
     */
    private String enhancementStrategy = "SUMMARIZATION";
    
    /**
     * 是否添加上下文（默认 true）
     */
    private Boolean addContext = true;
    
    /**
     * 上下文窗口大小（默认 3）
     */
    private Integer contextWindowSize = 3;
    
    /**
     * 最大文本长度（默认 2000）
     */
    private Integer maxLength = 2000;
}
