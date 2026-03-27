package com.shiyu.ai.agent.graph;

import com.shiyu.ai.agent.node.BaseNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.state.AgentState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Graph 类
 * 存储要构建 StateGraph 的所有属性和配置
 */
@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Graph {
    
    /**
     * 图名称
     */
    @Builder.Default
    private String name = "default_graph";
    
    /**
     * 图描述
     */
    @Builder.Default
    private String description = "";
    
    /**
     * 节点列表 (节点 ID -> 节点实例)
     */
    @Builder.Default
    private Map<String, BaseNode> nodes = new HashMap<>();
    
    /**
     * 边列表 (源节点 ID -> 目标节点 ID 列表)
     */
    @Builder.Default
    private Map<String, List<String>> edges = new HashMap<>();

    /**
     * 条件边列表 (源节点 ID -> ConditionEdge)
     */
    @Builder.Default
    private Map<String, ConditionEdge> conditionalEdges = new HashMap<>();
    
    /**
     * 起始节点 ID
     */
    @Builder.Default
    private String startNode = "";
    
    /**
     * 结束节点 ID
     */
    @Builder.Default
    private String endNode = "";
    
    /**
     * 编译后的图对象（用于缓存，避免重复编译）
     */
    @Builder.Default
    private CompiledGraph<AgentState> compiledGraph = null;
    
    /**
     * 添加节点
     * @param nodeId 节点 ID
     * @param node 节点实例
     * @return 当前 Graph 实例
     */
    public Graph addNode(String nodeId, BaseNode node) {
        this.nodes.put(nodeId, node);
        log.debug("添加节点：{}", nodeId);
        return this;
    }
    
    /**
     * 添加边
     * @param sourceId 源节点 ID
     * @param targetId 目标节点 ID
     * @return 当前 Graph 实例
     */
    public Graph addEdge(String sourceId, String targetId) {
        this.edges.computeIfAbsent(sourceId, k -> new ArrayList<>()).add(targetId);
        log.debug("添加边：{} -> {}", sourceId, targetId);
        return this;
    }
    
    /**
     * 添加条件边
     * @param sourceId 源节点 ID
     * @param conditionEdge 条件边对象
     * @return 当前 Graph 实例
     */
    public Graph addConditionalEdge(String sourceId, ConditionEdge conditionEdge) {
        this.conditionalEdges.put(sourceId, conditionEdge);
        log.debug("添加条件边：{}", sourceId);
        return this;
    }
    
    /**
     * 添加条件边（简化版）
     * @param sourceId 源节点 ID
     * @param condition 条件函数
     * @param mappings 条件映射 (条件结果 -> 目标节点 ID)
     * @return 当前 Graph 实例
     */
    public Graph addConditionalEdge(String sourceId, 
                                    Function<Map<String, Object>, String> condition,
                                    Map<String, String> mappings) {
        ConditionEdge conditionEdge = ConditionEdge.builder()
                .from(sourceId)
                .functionCondition(condition)
                .nodeMappings(mappings)
                .build();
        return addConditionalEdge(sourceId, conditionEdge);
    }
    
    /**
     * 设置起始节点
     * @param nodeId 节点 ID
     * @return 当前 Graph 实例
     */
    public Graph setStartNode(String nodeId) {
        this.startNode = nodeId;
        log.debug("设置起始节点：{}", nodeId);
        return this;
    }
    
    /**
     * 设置结束节点
     * @param nodeId 节点 ID
     * @return 当前 Graph 实例
     */
    public Graph setEndNode(String nodeId) {
        this.endNode = nodeId;
        log.debug("设置结束节点：{}", nodeId);
        return this;
    }
    
    /**
     * 验证图配置的完整性
     * @throws IllegalStateException 当配置不完整时
     */
    public void validate() {
        log.info("开始验证 Graph 配置：{}", this.name);
        
        // 1. 检查起始节点
        if (startNode == null || startNode.isEmpty()) {
            throw new IllegalStateException("起始节点未设置");
        }
        if (!nodes.containsKey(startNode)) {
            throw new IllegalStateException("起始节点 " + startNode + " 未在节点列表中定义");
        }
        
        // 2. 检查结束节点
        if (endNode != null && !endNode.isEmpty()) {
            if (!nodes.containsKey(endNode)) {
                throw new IllegalStateException("结束节点 " + endNode + " 未在节点列表中定义");
            }
        }
        
        // 3. 检查所有边的目标节点是否都存在
        for (Map.Entry<String, List<String>> entry : edges.entrySet()) {
            String sourceId = entry.getKey();
            if (!nodes.containsKey(sourceId)) {
                throw new IllegalStateException("边的源节点 " + sourceId + " 未定义");
            }
            for (String targetId : entry.getValue()) {
                if (!nodes.containsKey(targetId)) {
                    throw new IllegalStateException("边的目标节点 " + targetId + " 未定义");
                }
            }
        }
        
        // 4. 检查条件边的配置
        for (Map.Entry<String, ConditionEdge> entry : conditionalEdges.entrySet()) {
            String sourceId = entry.getKey();
            ConditionEdge conditionEdge = entry.getValue();
            
            if (!nodes.containsKey(sourceId)) {
                throw new IllegalStateException("条件边的源节点 " + sourceId + " 未定义");
            }
            
            // 检查条件边的目标节点映射
            for (String targetId : conditionEdge.getNodeMappings().values()) {
                if (!nodes.containsKey(targetId)) {
                    throw new IllegalStateException("条件边的目标节点 " + targetId + " 未定义");
                }
            }
        }
        
        log.info("Graph 配置验证通过：{}", this.name);
    }

    public CompiledGraph<AgentState> compile() throws GraphStateException {
        log.info("开始编译 Graph: {}", this.name);
        
        // 如果已经编译过，直接返回缓存的对象
        if (this.compiledGraph != null) {
            log.debug("Graph 已编译过，使用缓存的 CompiledGraph: {}", this.name);
            return this.compiledGraph;
        }
        
        // 执行编译
        this.compiledGraph = StateGraphBuilder.fromGraph(this).build();
        
        log.info("Graph 编译完成：{}", this.name);
        return this.compiledGraph;
    }
    
}
