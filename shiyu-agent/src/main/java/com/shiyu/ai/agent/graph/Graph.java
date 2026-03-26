package com.shiyu.ai.agent.graph;

import com.shiyu.ai.agent.config.NodeConfig;
import com.shiyu.ai.agent.node.BaseNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
     * 节点配置列表 (节点 ID -> 节点配置)
     */
    @Builder.Default
    private Map<String, NodeConfig> nodeConfigs = new HashMap<>();
    
    /**
     * 边列表 (源节点 ID -> 目标节点 ID 列表)
     */
    @Builder.Default
    private Map<String, List<String>> edges = new HashMap<>();
    
    /**
     * 条件边列表 (源节点 ID -> 条件映射)
     */
    @Builder.Default
    private Map<String, Map<String, String>> conditionalEdges = new HashMap<>();
    
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
     * 是否编译完成
     */
    @Builder.Default
    private Boolean compiled = false;
    
    /**
     * 添加节点
     * @param nodeId 节点 ID
     * @param node 节点实例
     * @return 当前 Graph 实例
     */
    public Graph addNode(String nodeId, BaseNode node) {
        this.nodes.put(nodeId, node);
        return this;
    }
    
    /**
     * 添加节点及配置
     * @param nodeId 节点 ID
     * @param node 节点实例
     * @param config 节点配置
     * @return 当前 Graph 实例
     */
    public Graph addNode(String nodeId, BaseNode node, NodeConfig config) {
        this.nodes.put(nodeId, node);
        this.nodeConfigs.put(nodeId, config);
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
        return this;
    }
    
    /**
     * 添加条件边
     * @param sourceId 源节点 ID
     * @param conditionMap 条件映射 (条件值 -> 目标节点 ID)
     * @return 当前 Graph 实例
     */
    public Graph addConditionalEdge(String sourceId, Map<String, String> conditionMap) {
        this.conditionalEdges.put(sourceId, conditionMap);
        return this;
    }
    
    /**
     * 设置起始节点
     * @param nodeId 节点 ID
     * @return 当前 Graph 实例
     */
    public Graph setStartNode(String nodeId) {
        this.startNode = nodeId;
        return this;
    }
    
    /**
     * 设置结束节点
     * @param nodeId 节点 ID
     * @return 当前 Graph 实例
     */
    public Graph setEndNode(String nodeId) {
        this.endNode = nodeId;
        return this;
    }
    
    /**
     * 获取节点
     * @param nodeId 节点 ID
     * @return 节点实例
     */
    public BaseNode getNode(String nodeId) {
        return this.nodes.get(nodeId);
    }
    
    /**
     * 获取节点配置
     * @param nodeId 节点 ID
     * @return 节点配置
     */
    public NodeConfig getNodeConfig(String nodeId) {
        return this.nodeConfigs.get(nodeId);
    }
    
    /**
     * 获取所有边的目标节点
     * @param sourceId 源节点 ID
     * @return 目标节点列表
     */
    public List<String> getTargets(String sourceId) {
        return this.edges.getOrDefault(sourceId, new ArrayList<>());
    }
    
    /**
     * 获取条件边映射
     * @param sourceId 源节点 ID
     * @return 条件映射
     */
    public Map<String, String> getConditionalEdge(String sourceId) {
        return this.conditionalEdges.get(sourceId);
    }
}
