package com.shiyu.ai.agent.implementation.graph;

import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeActionWithConfig.node_async;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeActorContext;
import com.shiyu.ai.agent.contract.node.NodeApprovalRequiredException;
import com.shiyu.ai.agent.implementation.checkpoint.AgentGraphCheckpointSaver;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 构建 State Graph 相关的对象、流程或运行时配置。
 */
@Slf4j
@Builder
public class StateGraphBuilder {

    /** 节点列表 (节点 ID -> 节点实例) */
    @Builder.Default private Map<String, BaseNode> nodes = new HashMap<>();

    /** 边列表 (源节点 ID -> 目标节点 ID 列表) */
    @Builder.Default private Map<String, List<String>> edges = new HashMap<>();

    /** 条件函数列表 (源节点 ID -> 条件函数) */
    @Builder.Default private Map<String, ConditionEdge> conditionalEdges = new HashMap<>();

    /** 通道列表 */
    @Builder.Default private Map<String, Channel<?>> channels = new HashMap<>();

    /** 起始节点 ID */
    @Builder.Default private String startNode = "";

    /** 结束节点 ID */
    @Builder.Default private String endNode = "";

    /**
     * 从 Graph 对象构建 StateGraphBuilder
     *
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
     *
     * @return 编译后的 CompiledGraph
     * @throws GraphStateException 图状态异常
     */
    public CompiledGraph<AgentState> build() throws GraphStateException {
        return build(CompileConfig.builder().build());
    }

    /**
     * 使用执行专属的检查点配置构建图。
     *
     * @param config 图编译配置
     * @return 编译后的图
     * @throws GraphStateException 图结构无效时抛出
     */
    public CompiledGraph<AgentState> build(CompileConfig config) throws GraphStateException {
        log.info("开始构建 StateGraph，节点数：{}", nodes.size());
        validateConditionalRoutes();

        // 创建 StateGraph 实例
        StateGraph<AgentState> stateGraph = new StateGraph<>(channels, AgentState::new);

        // 添加所有节点
        for (Map.Entry<String, BaseNode> entry : nodes.entrySet()) {
            String nodeId = entry.getKey();
            BaseNode node = entry.getValue();
            stateGraph.addNode(nodeId, node_async((state, runnableConfig) -> {
                    config.checkpointSaver().ifPresent(saver -> {
                        if (saver instanceof AgentGraphCheckpointSaver agentSaver) {
                            agentSaver.markNodeStarted(runnableConfig, nodeId, state.data());
                        }
                    });
                    Map<String, Object> nodeInput = new LinkedHashMap<>(state.data());
                    try {
                        Map<String, Object> nodeOutput = node.apply(nodeInput);
                        return NodeActorContext.publicData(nodeOutput);
                    } catch (NodeApprovalRequiredException pending) {
                        config.checkpointSaver().ifPresent(saver -> {
                            if (saver instanceof AgentGraphCheckpointSaver agentSaver) {
                                agentSaver.markApprovalPending(runnableConfig, nodeId,
                                        state.data(), pending.approvalId());
                            }
                        });
                        throw pending;
                    }
                }));
            log.debug("添加节点：nodeIdPresent={}", nodeId != null);
        }

        // 添加所有边
        for (Map.Entry<String, List<String>> entry : edges.entrySet()) {
            String sourceId = entry.getKey();
            List<String> targetIds = entry.getValue();

            if (targetIds == null || targetIds.isEmpty()) {
                throw new GraphStateException("边的目标列表不能为空: " + sourceId);
            }

            for (String targetId : targetIds) {
                stateGraph.addEdge(sourceId, targetId);
                log.debug(
                        "添加边：sourcePresent={}, targetPresent={}",
                        sourceId != null,
                        targetId != null);
            }
        }

        // 添加条件边
        for (Map.Entry<String, ConditionEdge> entry : conditionalEdges.entrySet()) {
            String sourceId = entry.getKey();
            ConditionEdge conditionEdge = entry.getValue();

            Map<String, String> mappings = new HashMap<>();
            if (conditionEdge.getNodeMappings() != null) {
                mappings.putAll(conditionEdge.getNodeMappings());
            }
            String defaultTarget = conditionEdge.getDefaultTarget();
            if (defaultTarget != null && !defaultTarget.isEmpty()) {
                mappings.putIfAbsent(defaultTarget, defaultTarget);
            }
            if (!mappings.isEmpty()) {
                stateGraph.addConditionalEdges(
                        sourceId,
                        edge_async(state -> conditionEdge.getTarget(state.data())),
                        mappings);
                log.debug("添加条件边：sourcePresent={}", sourceId != null);
            }
        }

        // 添加起始边
        if (startNode != null && !startNode.isEmpty()) {
            stateGraph.addEdge(StateGraph.START, startNode);
            log.debug("添加起始边：startNodePresent={}", startNode != null);
        }

        // 添加结束边
        if (endNode != null && !endNode.isEmpty()) {
            stateGraph.addEdge(endNode, StateGraph.END);
            log.debug("添加结束边：endNodePresent={}", endNode != null);
        }

        // 编译并返回
        log.info("StateGraph 构建完成，开始编译...");
        return stateGraph.compile(config);
    }

    private void validateConditionalRoutes() throws GraphStateException {
        for (Map.Entry<String, ConditionEdge> entry : conditionalEdges.entrySet()) {
            String sourceId = entry.getKey();
            ConditionEdge route = entry.getValue();
            if (!nodes.containsKey(sourceId) || route == null) {
                throw new GraphStateException("条件边的源节点不存在或路由为空: " + sourceId);
            }
            String defaultTarget = route.getDefaultTarget();
            if ((defaultTarget == null || defaultTarget.isEmpty())
                    && (route.getNodeMappings() == null || route.getNodeMappings().isEmpty())) {
                throw new GraphStateException("条件边没有可用的目标节点: " + sourceId);
            }
            if (defaultTarget != null && !defaultTarget.isEmpty()
                    && !nodes.containsKey(defaultTarget)) {
                throw new GraphStateException("条件边的默认目标节点不存在: " + defaultTarget);
            }
            if (route.getNodeMappings() != null) {
                for (String targetId : route.getNodeMappings().values()) {
                    if (!nodes.containsKey(targetId)) {
                        throw new GraphStateException("条件边的目标节点不存在: " + targetId);
                    }
                }
            }
        }
    }
}
