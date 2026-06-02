package com.shiyu.ai.agent.langgraph4j.graph;

import com.shiyu.ai.agent.langgraph4j.node.BaseNode;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * StateGraph 构建器
 * 用于将 Graph 对象转换为 langgraph4j 的 StateGraph
 */
@Slf4j
@Builder
public class StateGraphBuilder {

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
     * 条件函数列表 (源节点 ID -> 条件函数)
     */
    @Builder.Default
    private Map<String, ConditionEdge> conditionalEdges = new HashMap<>();

    /**
     * 通道列表
     */
    @Builder.Default
    private Map<String, Channel<?>> channels = new HashMap<>();

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
     * 从 Graph 对象构建 StateGraphBuilder
     * @param graph Graph 对象
     * @return StateGraphBuilder 实例
     */
    public static StateGraphBuilder fromGraph(Graph graph) {
        log.info("开始从 Graph 构建 StateGraphBuilder: {}", graph.getName());
        
        // 先验证配置
        graph.validate();
        
        return StateGraphBuilder.builder()
                .nodes(graph.getNodes())
                .edges(graph.getEdges())
                .conditionalEdges(graph.getConditionalEdges())
                .channels(graph.getChannels())
                .startNode(graph.getStartNode())
                .endNode(graph.getEndNode())
                .build();
    }

    /**
     * 构建并编译 StateGraph
     * @return 编译后的 CompiledGraph
     * @throws GraphStateException 图状态异常
     */
    public CompiledGraph<AgentState> build() throws GraphStateException {
        log.info("开始构建 StateGraph，节点数：{}", nodes.size());
        
        // 创建 StateGraph 实例
        StateGraph<AgentState> stateGraph = new StateGraph<>(channels, AgentState::new);
        
        // 添加所有节点
        for (Map.Entry<String, BaseNode> entry : nodes.entrySet()) {
            String nodeId = entry.getKey();
            BaseNode node = entry.getValue();
            try {
                stateGraph.addNode(nodeId, node_async(node));
            } catch (GraphStateException e) {
                log.error("添加节点失败：{}", nodeId, e);
                throw e;
            }
            log.debug("添加节点：{}", nodeId);
        }
        
        // 添加所有边
        for (Map.Entry<String, List<String>> entry : edges.entrySet()) {
            String sourceId = entry.getKey();
            List<String> targetIds = entry.getValue();
            
            for (String targetId : targetIds) {
                try {
                    stateGraph.addEdge(sourceId, targetId);
                } catch (GraphStateException e) {
                    log.error("添加边失败：{} -> {}", sourceId, targetId, e);
                    throw e;
                }
                log.debug("添加边：{} -> {}", sourceId, targetId);
            }
        }
        
        // 添加条件边
        for (Map.Entry<String, ConditionEdge> entry : conditionalEdges.entrySet()) {
            String sourceId = entry.getKey();
            ConditionEdge conditionEdge = entry.getValue();

            Map<String, String> mappings = conditionEdge.getNodeMappings();
            if (!mappings.isEmpty()) {
                try {
                    stateGraph.addConditionalEdges(sourceId,
                            edge_async(state -> conditionEdge.getTarget(state.data()))
                            , mappings);
                } catch (GraphStateException e) {
                    log.error("添加条件边失败：{}", sourceId, e);
                    throw e;
                }
                log.debug("添加条件边：{}", sourceId);
            }
        }
        
        // 添加起始边
        if (startNode != null && !startNode.isEmpty()) {
            try {
                stateGraph.addEdge(StateGraph.START, startNode);
            } catch (GraphStateException e) {
                log.error("添加起始边失败：START -> {}", startNode, e);
                throw e;
            }
            log.debug("添加起始边：START -> {}", startNode);
        }
        
        // 添加结束边
        if (endNode != null && !endNode.isEmpty()) {
            try {
                stateGraph.addEdge(endNode, StateGraph.END);
            } catch (GraphStateException e) {
                log.error("添加结束边失败：{} -> END", endNode, e);
                throw e;
            }
            log.debug("添加结束边：{} -> END", endNode);
        }
        
        // 编译并返回
        log.info("StateGraph 构建完成，开始编译...");
        return stateGraph.compile();
    }
}
