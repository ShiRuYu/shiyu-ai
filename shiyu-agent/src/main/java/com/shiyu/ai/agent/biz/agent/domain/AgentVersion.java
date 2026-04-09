package com.shiyu.ai.agent.biz.agent.domain;

import com.shiyu.ai.agent.langgraph4j.graph.Graph;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Agent 版本定义类
 * 包含版本信息和对应的 Graph
 */
@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentVersion {
    
    /**
     * 版本号（如：v1.0.0, v1.0.1）
     */
    private String versionNumber;
    
    /**
     * 版本描述
     */
    private String description;
    
    /**
     * 图定义
     */
    private Graph graph;
    
    /**
     * 创建时间
     */
    private Long createdAt;
    
    /**
     * 是否已编译
     * @return true-已编译，false-未编译
     */
    public boolean isCompiled() {
        return graph != null && graph.isCompiled();
    }
}
