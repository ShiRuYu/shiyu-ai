package com.shiyu.ai.agent.langgraph4j.graph;

import com.shiyu.ai.agent.langgraph4j.node.BaseNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
     * 编译后的图对象（用于缓存，避免重复编译）
     */
    @Builder.Default
    private CompiledGraph<AgentState> compiledGraph = null;

    /**
     * 批量添加节点（仅在构建阶段使用）
     */
    public void addAllNodes(Map<String, BaseNode> nodes) {
        this.nodes.putAll(nodes);
    }

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
     * 添加通道
     * @param name 通道名称
     * @param channel 通道对象
     * @return 当前 Graph 实例
     */
    public Graph addChannel(String name, Channel<?> channel) {
        this.channels.put(name, channel);
        log.debug("添加通道：{}", name);
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

        // 5. 检测循环依赖（DFS）
        String cycle = detectCycle();
        if (cycle != null) {
            throw new IllegalStateException("图检测到循环依赖：" + cycle);
        }

        // 6. 检测不可达节点
        Set<String> unreachable = findUnreachableNodes();
        if (!unreachable.isEmpty()) {
            log.warn("图存在不可达节点：{}（不影响执行，但可能表明配置遗漏）", unreachable);
        }
        
        log.info("Graph 配置验证通过：{}", this.name);
    }

    /**
     * 是否已编译
     * @return true-已编译，false-未编译
     */
    public boolean isCompiled() {
        return this.compiledGraph != null;
    }
    
    /**
     * 重新编译 Graph
     * 用于清除缓存并重新编译
     * @return CompiledGraph 实例
     * @throws GraphStateException 编译异常
     */
    public synchronized CompiledGraph<AgentState> recompile() throws GraphStateException {
        log.info("重新编译 Graph: {}", this.name);
        this.compiledGraph = null;
        return compile();
    }
    
    /**
     * 验证 Graph 配置
     * @return true-配置有效，false-配置无效
     */
    public boolean validateGraph() {
        try {
            validate();
            log.info("Graph 配置验证通过：{}", this.name);
            return true;
        } catch (Exception e) {
            log.error("Graph 配置验证失败：{}", this.name, e);
            return false;
        }
    }

    public synchronized CompiledGraph<AgentState> compile() throws GraphStateException {
        log.info("开始编译 Graph: {}", this.name);
        
        if (this.compiledGraph != null) {
            log.debug("Graph 已编译过，使用缓存的 CompiledGraph: {}", this.name);
            return this.compiledGraph;
        }
        
        this.compiledGraph = StateGraphBuilder.fromGraph(this).build();
        
        log.info("Graph 编译完成：{}", this.name);
        return this.compiledGraph;
    }
    
    /**
     * 同步执行图
     * @param input 输入数据
     * @return 执行结果
     * @throws GraphStateException 图状态异常
     */
    public Map<String, Object> execute(Map<String, Object> input) throws GraphStateException {
        log.info("开始同步执行图：{}", this.name);
        
        CompiledGraph<AgentState> compiledGraph = compile();
        
        // 执行图并获取最终状态
        var resultOptional = compiledGraph.invoke(input);
        AgentState finalState = resultOptional.orElseThrow(() -> 
            new IllegalStateException("图执行返回空结果：" + this.name));
        
        log.info("图同步执行完成：{}", this.name);
        return finalState.data();
    }
    
    /**
     * 流式执行图
     * @param input 输入数据
     * @return 流式响应
     * @throws GraphStateException 图状态异常
     */
    public Flux<Map<String, Object>> executeStream(Map<String, Object> input) throws GraphStateException {
        log.info("开始流式执行图：{}", this.name);
        
        CompiledGraph<AgentState> compiledGraph = compile();
        
        // 流式执行图 - 返回每个节点执行后的状态
        return Flux.fromIterable(() -> compiledGraph.stream(input).iterator())
                .map(nodeOutput -> nodeOutput.state().data())
                .doOnSubscribe(subscription -> log.debug("流式执行开始"))
                .doOnComplete(() -> log.info("流式执行完成"))
                .doOnError(error -> log.error("流式执行失败", error));
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
        ConditionEdge condEdge = conditionalEdges.get(node);
        if (condEdge != null && condEdge.getNodeMappings() != null) {
            successors.addAll(condEdge.getNodeMappings().values());
        }

        for (String successor : successors) {
            if (black.contains(successor)) continue;
            if (gray.contains(successor)) return true;
            if (hasCycle(successor, white, gray, black)) return true;
        }

        gray.remove(node);
        black.add(node);
        return false;
    }

    /**
     * 查找从起始节点无法到达的节点
     */
    Set<String> findUnreachableNodes() {
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
            for (String target : condEdge.getNodeMappings().values()) dfsReachable(target, reachable);
        }
    }

}
