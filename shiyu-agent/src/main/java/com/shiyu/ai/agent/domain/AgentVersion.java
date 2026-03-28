package com.shiyu.ai.agent.domain;

import com.shiyu.ai.agent.graph.Graph;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.state.AgentState;

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
        return graph != null && graph.getCompiledGraph() != null;
    }
    
    /**
     * 获取或编译 Graph
     * @return CompiledGraph 实例
     * @throws GraphStateException 编译异常
     */
    public CompiledGraph<AgentState> getOrCompileGraph() throws GraphStateException {
        if (graph == null) {
            log.error("Graph 未定义：version={}", versionNumber);
            throw new GraphStateException("Graph 未定义，无法编译");
        }
            
        // 直接使用 Graph 内部的缓存机制
        log.info("开始编译/获取缓存的 Graph: version={}", versionNumber);
        CompiledGraph<AgentState> result = graph.compile();
        log.info("Graph 编译完成：version={}", versionNumber);
            
        return result;
    }
    
    /**
     * 重新编译 Graph
     * 用于清除缓存并重新编译
     * @return CompiledGraph 实例
     * @throws GraphStateException 编译异常
     */
    public CompiledGraph<AgentState> recompileGraph() throws GraphStateException {
        log.info("重新编译 Graph: version={}", versionNumber);
            
        if (graph == null) {
            throw new GraphStateException("Graph 未定义，无法重新编译");
        }
            
        // 清除 Graph 内部的缓存并重新编译
        graph.setCompiledGraph(null);
        return getOrCompileGraph();
    }
    
    /**
     * 验证 Graph 配置
     * @return true-配置有效，false-配置无效
     */
    public boolean validateGraph() {
        if (graph == null) {
            log.error("Graph 未定义：version={}", versionNumber);
            return false;
        }
        
        try {
            graph.validate();
            log.info("Graph 配置验证通过：version={}", versionNumber);
            return true;
        } catch (Exception e) {
            log.error("Graph 配置验证失败：version={}", versionNumber, e);
            return false;
        }
    }
}
