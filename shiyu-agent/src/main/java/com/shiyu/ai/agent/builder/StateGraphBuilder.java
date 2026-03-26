package com.shiyu.ai.agent.builder;

import com.shiyu.ai.agent.config.NodeConfig;
import com.shiyu.ai.agent.graph.Graph;
import com.shiyu.ai.agent.node.BaseNode;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * StateGraph 构建器
 * 用于将 Graph 对象转换为 langgraph4j 的 StateGraph
 */
@Slf4j
public class StateGraphBuilder {
    
    /**
     * 创建 StateGraphBuilder 实例
     * @param stateFactory AgentState 工厂方法
     * @return StateGraphBuilder
     */
    public static StateGraphBuilder create(Function<Map<String, Object>, ? extends AgentState> stateFactory) {
        return new StateGraphBuilder(stateFactory);
    }
    
    private final Function<Map<String, Object>, ? extends AgentState> stateFactory;
    
    /**
     * 构造函数
     * @param stateFactory AgentState 工厂方法
     */
    public StateGraphBuilder(Function<Map<String, Object>, ? extends AgentState> stateFactory) {
        this.stateFactory = stateFactory;
    }
    
    /**
     * 构建 StateGraph
     * @param graph 图配置
     * @return StateGraph
     */
    public <T extends AgentState> StateGraph<T> build(Graph graph) {
        // 创建状态图 (直接使用 AgentState)
        StateGraph<T> stateGraph = new StateGraph<>((Class<T>) AgentState.class);
        
        // 添加所有节点
        addNodes(stateGraph, graph);
        
        // 添加所有边
        addEdges(stateGraph, graph);
        
        // 添加条件边
        addConditionalEdges(stateGraph, graph);
        
        // 设置起始和结束节点
        setStartAndEndNodes(stateGraph, graph);
        
        log.info("StateGraph 构建完成：{}", graph.getName());
        
        return stateGraph;
    }
    
    /**
     * 添加节点到 StateGraph
     * @param stateGraph StateGraph
     * @param graph Graph 对象
     */
    private void addNodes(StateGraph<?> stateGraph, Graph graph) {
        for (Map.Entry<String, BaseNode> entry : graph.getNodes().entrySet()) {
            String nodeId = entry.getKey();
            BaseNode node = entry.getValue();
            NodeConfig config = graph.getNodeConfig(nodeId);
            
            // 应用节点配置
            applyNodeConfig(node, config);
            
            // 添加节点
            stateGraph.addNode(nodeId, node);
            
            log.debug("添加节点：{}", nodeId);
        }
    }
    
    /**
     * 应用节点配置
     * @param node 节点实例
     * @param config 节点配置
     */
    private void applyNodeConfig(BaseNode node, NodeConfig config) {
        if (config != null && config.getEnabled()) {
            node.setConfig(config);
            log.debug("应用节点配置：{}", config.getNodeName());
        }
    }
    
    /**
     * 添加边到 StateGraph
     * @param stateGraph StateGraph
     * @param graph Graph 对象
     */
    private void addEdges(StateGraph<?> stateGraph, Graph graph) {
        for (Map.Entry<String, List<String>> entry : graph.getEdges().entrySet()) {
            String sourceId = entry.getKey();
            List<String> targets = entry.getValue();
            
            for (String targetId : targets) {
                stateGraph.addEdge(sourceId, targetId);
                log.debug("添加边：{} -> {}", sourceId, targetId);
            }
        }
    }
    
    /**
     * 添加条件边到 StateGraph
     * @param stateGraph StateGraph
     * @param graph Graph 对象
     */
    private void addConditionalEdges(StateGraph<?> stateGraph, Graph graph) {
        if (graph.getConditionalEdges() != null && !graph.getConditionalEdges().isEmpty()) {
            for (Map.Entry<String, Map<String, String>> entry : graph.getConditionalEdges().entrySet()) {
                String sourceId = entry.getKey();
                Map<String, String> conditionMap = entry.getValue();
                
                // 将条件映射添加到 stateGraph
                stateGraph.addConditionalEdge(sourceId, conditionMap);
                log.debug("添加条件边：{} -> {}", sourceId, conditionMap);
            }
        }
    }
    
    /**
     * 设置起始和结束节点
     * @param stateGraph StateGraph
     * @param graph Graph 对象
     */
    private void setStartAndEndNodes(StateGraph<?> stateGraph, Graph graph) {
        if (graph.getStartNode() != null && !graph.getStartNode().isEmpty()) {
            stateGraph.addEdge(StateGraph.START, graph.getStartNode());
            log.debug("设置起始节点：{}", graph.getStartNode());
        }
        
        if (graph.getEndNode() != null && !graph.getEndNode().isEmpty()) {
            stateGraph.addEdge(graph.getEndNode(), StateGraph.END);
            log.debug("设置结束节点：{}", graph.getEndNode());
        }
    }
}
