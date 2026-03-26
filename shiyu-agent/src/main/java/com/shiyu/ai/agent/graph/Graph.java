package com.shiyu.ai.agent.graph;

import com.shiyu.ai.agent.builder.StateGraphBuilder;
import com.shiyu.ai.agent.config.NodeConfig;
import com.shiyu.ai.agent.node.BaseNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
     * 条件边列表 (源节点 ID -> (条件结果 -> 目标节点 ID))
     */
    @Builder.Default
    private Map<String, Map<String, String>> conditionalEdgeMappings = new HashMap<>();
    
    /**
     * 条件函数列表 (源节点 ID -> 条件函数)
     */
    @Builder.Default
    private Map<String, AsyncEdgeAction<AgentState>> conditionalFunctions = new HashMap<>();
    
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
     * @param condition 条件函数
     * @param mappings 条件映射 (条件结果 -> 目标节点 ID)
     * @return 当前 Graph 实例
     */
    public Graph addConditionalEdge(String sourceId, 
                                    AsyncEdgeAction<AgentState> condition,
                                    Map<String, String> mappings) {
        this.conditionalFunctions.put(sourceId, condition);
        this.conditionalEdgeMappings.put(sourceId, mappings);
        log.debug("添加条件边：{}", sourceId);
        return this;
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
     * 构建并编译 StateGraph
     * @return 编译后的 CompiledGraph
     * @throws org.bsc.langgraph4j.GraphStateException 图状态异常
     */
    public org.bsc.langgraph4j.CompiledGraph<AgentState> compile() throws org.bsc.langgraph4j.GraphStateException {
        log.info("开始编译 Graph: {}", this.name);
        return StateGraphBuilder.fromGraph(this).build();
    }
    
}
