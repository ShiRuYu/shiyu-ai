package com.shiyu.ai.agent.implementation.graph;

import com.shiyu.ai.agent.contract.node.BaseNode;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.bsc.async.AsyncGenerator;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;

import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** Graph 类 存储要构建 StateGraph 的所有属性和配置 */
@Slf4j
@Data
@Builder
@NoArgsConstructor
public class Graph {

    /** Graph名称 */
    @Builder.Default private String name = "default_graph";

    /** Graph描述 */
    @Builder.Default private String description = "";

    /** 节点列表 (节点 ID -> 节点实例) */
    @Builder.Default private Map<String, BaseNode> nodes = new HashMap<>();

    /** 边列表 (源节点 ID -> 目标节点 ID 列表) */
    @Builder.Default private Map<String, List<String>> edges = new HashMap<>();

    /** 条件边列表 (源节点 ID -> ConditionEdge) */
    @Builder.Default private Map<String, ConditionEdge> conditionalEdges = new HashMap<>();

    /** 通道列表 */
    @Builder.Default private Map<String, Channel<?>> channels = new HashMap<>();

    /** 起始节点 ID */
    @Builder.Default private String startNode = "";

    /** 结束节点 ID */
    @Builder.Default private String endNode = "";

    /** 编译后的Graph对象（用于缓存，避免重复编译） */
    @Builder.Default private CompiledGraph<AgentState> compiledGraph = null;

    public Graph(
            String name,
            String description,
            Map<String, BaseNode> nodes,
            Map<String, List<String>> edges,
            Map<String, ConditionEdge> conditionalEdges,
            Map<String, Channel<?>> channels,
            String startNode,
            String endNode,
            CompiledGraph<AgentState> compiledGraph) {
        this.name = name;
        this.description = description;
        this.nodes = mutableCopy(nodes);
        this.edges = mutableEdgeCopy(edges);
        this.conditionalEdges = mutableCopy(conditionalEdges);
        this.channels = mutableCopy(channels);
        this.startNode = startNode;
        this.endNode = endNode;
        this.compiledGraph = compiledGraph;
    }

    /** 批量添加节点（仅在构建阶段使用） */
    public void addAllNodes(Map<String, BaseNode> nodes) {
        Map<String, BaseNode> updated = mutableCopy(this.nodes);
        updated.putAll(nodes);
        this.nodes = updated;
        invalidateCompiledGraph();
    }

    /** 设置节点集合时复制输入，避免 Graph.builder() 接收 Map.of 等不可变集合后无法继续编辑。 */
    public void setNodes(Map<String, BaseNode> nodes) {
        this.nodes = mutableCopy(nodes);
        invalidateCompiledGraph();
    }

    public void setEdges(Map<String, List<String>> edges) {
        this.edges = mutableEdgeCopy(edges);
        invalidateCompiledGraph();
    }

    public void setConditionalEdges(Map<String, ConditionEdge> conditionalEdges) {
        this.conditionalEdges = mutableCopy(conditionalEdges);
        invalidateCompiledGraph();
    }

    public void setChannels(Map<String, Channel<?>> channels) {
        this.channels = mutableCopy(channels);
        invalidateCompiledGraph();
    }

    public Map<String, BaseNode> getNodes() {
        return Collections.unmodifiableMap(new HashMap<>(nodes));
    }

    public Map<String, List<String>> getEdges() {
        Map<String, List<String>> copy = new HashMap<>();
        edges.forEach(
                (sourceId, targets) ->
                        copy.put(
                                sourceId,
                                targets == null
                                        ? null
                                        : Collections.unmodifiableList(new ArrayList<>(targets))));
        return Collections.unmodifiableMap(copy);
    }

    public Map<String, ConditionEdge> getConditionalEdges() {
        return Collections.unmodifiableMap(new HashMap<>(conditionalEdges));
    }

    public Map<String, Channel<?>> getChannels() {
        return Collections.unmodifiableMap(new HashMap<>(channels));
    }

    /**
     * 添加节点
     *
     * @param nodeId 节点 ID
     * @param node 节点实例
     * @return 当前 Graph 实例
     */
    public Graph addNode(String nodeId, BaseNode node) {
        this.nodes = mutableCopy(this.nodes);
        this.nodes.put(nodeId, node);
        invalidateCompiledGraph();
        log.debug("添加节点：nodeIdPresent={}", nodeId != null);
        return this;
    }

    /**
     * 添加边
     *
     * @param sourceId 源节点 ID
     * @param targetId 目标节点 ID
     * @return 当前 Graph 实例
     */
    public Graph addEdge(String sourceId, String targetId) {
        this.edges = mutableCopy(this.edges);
        List<String> targets = new ArrayList<>(this.edges.getOrDefault(sourceId, List.of()));
        targets.add(targetId);
        this.edges.put(sourceId, targets);
        invalidateCompiledGraph();
        log.debug("添加边：sourcePresent={}, targetPresent={}", sourceId != null, targetId != null);
        return this;
    }

    /**
     * 添加条件边
     *
     * @param sourceId 源节点 ID
     * @param conditionEdge 条件边对象
     * @return 当前 Graph 实例
     */
    public Graph addConditionalEdge(String sourceId, ConditionEdge conditionEdge) {
        this.conditionalEdges = mutableCopy(this.conditionalEdges);
        this.conditionalEdges.put(sourceId, conditionEdge);
        invalidateCompiledGraph();
        log.debug("添加条件边：sourcePresent={}", sourceId != null);
        return this;
    }

    /**
     * 添加条件边（简化版）
     *
     * @param sourceId 源节点 ID
     * @param condition 条件函数
     * @param mappings 条件映射 (条件结果 -> 目标节点 ID)
     * @return 当前 Graph 实例
     */
    public Graph addConditionalEdge(
            String sourceId,
            Function<Map<String, Object>, String> condition,
            Map<String, String> mappings) {
        ConditionEdge conditionEdge =
                ConditionEdge.builder()
                        .from(sourceId)
                        .functionCondition(condition)
                        .nodeMappings(mappings)
                        .build();
        return addConditionalEdge(sourceId, conditionEdge);
    }

    /**
     * 添加通道
     *
     * @param name 通道名称
     * @param channel 通道对象
     * @return 当前 Graph 实例
     */
    public Graph addChannel(String name, Channel<?> channel) {
        this.channels = mutableCopy(this.channels);
        this.channels.put(name, channel);
        invalidateCompiledGraph();
        log.debug("添加通道：namePresent={}", name != null);
        return this;
    }

    /**
     * 设置起始节点
     *
     * @param nodeId 节点 ID
     * @return 当前 Graph 实例
     */
    public Graph setStartNode(String nodeId) {
        this.startNode = nodeId;
        invalidateCompiledGraph();
        log.debug("设置起始节点：nodeIdPresent={}", nodeId != null);
        return this;
    }

    /**
     * 设置结束节点
     *
     * @param nodeId 节点 ID
     * @return 当前 Graph 实例
     */
    public Graph setEndNode(String nodeId) {
        this.endNode = nodeId;
        invalidateCompiledGraph();
        log.debug("设置结束节点：nodeIdPresent={}", nodeId != null);
        return this;
    }

    /**
     * 验证Graph配置的完整性
     *
     * @throws IllegalStateException 当配置不完整时
     */
    public void validate() {
        log.info("开始验证 Graph 配置：namePresent={}", this.name != null);

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
            List<String> targets = entry.getValue();
            if (targets == null) {
                throw new IllegalStateException("边 " + sourceId + " 的目标列表不能为空");
            }
            for (String targetId : targets) {
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
            if (conditionEdge == null) {
                throw new IllegalStateException("条件边 " + sourceId + " 不能为空");
            }

            // 检查条件边的目标节点映射
            Map<String, String> mappings = conditionEdge.getNodeMappings();
            if (mappings == null) {
                continue;
            }
            for (String targetId : mappings.values()) {
                if (!nodes.containsKey(targetId)) {
                    throw new IllegalStateException("条件边的目标节点 " + targetId + " 未定义");
                }
            }
        }

        // 5. 检测循环依赖（DFS）
        String cycle = detectCycle();
        if (cycle != null) {
            throw new IllegalStateException("Graph检测到循环依赖：" + cycle);
        }

        // 6. 检测不可达节点
        Set<String> unreachable = findUnreachableNodes();
        if (!unreachable.isEmpty()) {
            log.warn("Graph存在不可达节点：count={}（不影响执行，但可能表明配置遗漏）", unreachable.size());
        }

        log.info("Graph 配置验证通过：namePresent={}", this.name != null);
    }

    /**
     * 是否已编译
     *
     * @return true-已编译，false-未编译
     */
    public boolean isCompiled() {
        return this.compiledGraph != null;
    }

    /**
     * 重新编译 Graph 用于清除缓存并重新编译
     *
     * @return CompiledGraph 实例
     * @throws GraphStateException 编译异常
     */
    public synchronized CompiledGraph<AgentState> recompile() throws GraphStateException {
        log.info("重新编译 Graph: namePresent={}", this.name != null);
        this.compiledGraph = null;
        return compile();
    }

    /**
     * 验证 Graph 配置
     *
     * @return true-配置有效，false-配置无效
     */
    public boolean validateGraph() {
        try {
            validate();
            log.info("Graph 配置验证通过：namePresent={}", this.name != null);
            return true;
        } catch (Exception e) {
            log.error(
                    "Graph 配置验证失败：namePresent={}, errorType={}, errorMessageLength={}",
                    this.name != null,
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
            return false;
        }
    }

    public synchronized CompiledGraph<AgentState> compile() throws GraphStateException {
        log.info("开始编译 Graph: namePresent={}", this.name != null);

        if (this.compiledGraph != null) {
            log.debug("Graph 已编译过，使用缓存的 CompiledGraph: namePresent={}", this.name != null);
            return this.compiledGraph;
        }

        this.compiledGraph = StateGraphBuilder.fromGraph(this).build();

        log.info("Graph 编译完成：namePresent={}", this.name != null);
        return this.compiledGraph;
    }

    /**
     * 编译并获取流式执行源（返回 AsyncGenerator，由调用方控制消费逻辑）
     *
     * @param input 输入数据
     * @return AsyncGenerator 流式源
     * @throws GraphStateException Graph状态异常
     */
    public AsyncGenerator<NodeOutput<AgentState>> stream(Map<String, Object> input)
            throws GraphStateException {
        log.info("开始流式执行Graph：namePresent={}", this.name != null);
        CompiledGraph<AgentState> compiledGraph = compile();
        return compiledGraph.stream(input);
    }

    /**
     * 同步执行Graph
     *
     * @param input 输入数据
     * @return 执行结果
     * @throws GraphStateException Graph状态异常
     */
    public Map<String, Object> execute(Map<String, Object> input) throws GraphStateException {
        log.info("开始同步执行Graph：namePresent={}", this.name != null);

        CompiledGraph<AgentState> compiledGraph = compile();

        // 执行Graph并获取最终状态
        var resultOptional = compiledGraph.invoke(input);
        AgentState finalState =
                resultOptional.orElseThrow(
                        () -> new IllegalStateException("Graph执行返回空结果：" + this.name));

        log.info("Graph同步执行完成：namePresent={}", this.name != null);
        return finalState.data();
    }

    /**
     * 流式执行Graph
     *
     * @param input 输入数据
     * @return 流式响应
     * @throws GraphStateException Graph状态异常
     */
    public Flux<NodeOutput<AgentState>> executeStream(Map<String, Object> input)
            throws GraphStateException {
        log.info("开始流式执行Graph：namePresent={}", this.name != null);

        CompiledGraph<AgentState> compiledGraph = compile();

        // 流式执行Graph - 返回每个节点执行后的状态
        return Flux.fromIterable(() -> compiledGraph.stream(input).iterator())
                .doOnSubscribe(
                        subscription -> log.debug("Graph 流式执行开始：namePresent={}", this.name != null))
                .doOnComplete(() -> log.info("Graph 流式执行完成：namePresent={}", this.name != null))
                .doOnError(
                        error ->
                                log.error(
                                        "Graph 流式执行失败：namePresent={}, errorType={},"
                                                + " errorMessageLength={}",
                                        this.name != null,
                                        error.getClass().getSimpleName(),
                                        error.getMessage() == null
                                                ? 0
                                                : error.getMessage().length()));
    }

    /**
     * 基于 DFS 的循环依赖检测
     *
     * @return 检测到循环时返回路径描述字符串，否则返回 null
     */
    String detectCycle() {
        Set<String> white = new HashSet<>(nodes.keySet());
        Set<String> gray = new LinkedHashSet<>();
        Set<String> black = new HashSet<>();

        for (String node : new ArrayList<>(white)) {
            if (black.contains(node)) continue;
            if (hasCycle(node, white, gray, black)) {
                List<String> path = new ArrayList<>(gray);
                int start = path.indexOf(node);
                return String.join(" -> ", path.subList(start, path.size())) + " -> " + node;
            }
        }
        return null;
    }

    private boolean hasCycle(String node, Set<String> white, Set<String> gray, Set<String> black) {
        white.remove(node);
        gray.add(node);

        Set<String> successors = new HashSet<>();
        List<String> edgeTargets = edges.get(node);
        if (edgeTargets != null) successors.addAll(edgeTargets);
        // 条件边不参与循环检测 — 它们是"有时"路径，不是"总是"路径
        // 例如 tutor-graph 的 scoreAnalysis→teach（仅 score<60 时走）

        for (String successor : successors) {
            if (black.contains(successor)) continue;
            if (gray.contains(successor)) return true;
            if (hasCycle(successor, white, gray, black)) return true;
        }

        gray.remove(node);
        black.add(node);
        return false;
    }

    /** 查找从起始节点无法到达的节点 */
    public Set<String> findUnreachableNodes() {
        Set<String> reachable = new HashSet<>();
        dfsReachable(startNode, reachable);
        Set<String> unreachable = new HashSet<>(nodes.keySet());
        unreachable.removeAll(reachable);
        unreachable.remove(startNode);
        return unreachable;
    }

    private void dfsReachable(String node, Set<String> reachable) {
        if (node == null || node.isEmpty() || reachable.contains(node)) return;
        reachable.add(node);
        List<String> edgeTargets = edges.get(node);
        if (edgeTargets != null) {
            for (String target : edgeTargets) dfsReachable(target, reachable);
        }
        ConditionEdge condEdge = conditionalEdges.get(node);
        if (condEdge != null && condEdge.getNodeMappings() != null) {
            for (String target : condEdge.getNodeMappings().values())
                dfsReachable(target, reachable);
        }
    }

    private void invalidateCompiledGraph() {
        this.compiledGraph = null;
    }

    private static <K, V> Map<K, V> mutableCopy(Map<K, V> source) {
        return source == null ? new HashMap<>() : new HashMap<>(source);
    }

    private static Map<String, List<String>> mutableEdgeCopy(Map<String, List<String>> source) {
        Map<String, List<String>> copy = new HashMap<>();
        if (source != null) {
            source.forEach(
                    (sourceId, targets) ->
                            copy.put(sourceId, targets == null ? null : new ArrayList<>(targets)));
        }
        return copy;
    }
}
