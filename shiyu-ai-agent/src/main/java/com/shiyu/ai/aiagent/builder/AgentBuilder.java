package com.shiyu.ai.aiagent.builder;

import com.shiyu.ai.aiagent.service.AgentService;
import com.shiyu.ai.aiagent.AgentDefinition;
import com.shiyu.ai.aiagent.AgentVersion;
import com.shiyu.ai.aiagent.graph.ConditionEdge;
import com.shiyu.ai.aiagent.graph.Graph;
import com.shiyu.ai.aiagent.node.BaseNode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Agent Builder
 * 用于便捷构建和注册 Agent 的工具类
 * 
 * 使用示例:
 * <pre>
 * {@code
 * AgentBuilder builder = new AgentBuilder();
 * AgentDefinition agent = builder
 *     .agentId("customer-service-agent")
 *     .name("客服助手")
 *     .description("智能客服问答助手")
 *     .version("v1.0.0")
 *     .addNode("start", new InputNode())
 *     .addNode("intent", IntentNode.builder().config(intentConfig).intentService(intentService).build())
 *     .addNode("llm", LlmCallNode.builder().config(llmConfig).intentService(llmService).build())
 *     .addNode("end", new OutputNode())
 *     .setStartNode("start")
 *     .setEndNode("end")
 *     .addEdge("start", "intent")
 *     .addEdge("intent", "llm")
 *     .addEdge("llm", "end")
 *     .buildAndRegister(agentService);
 * }
 * </pre>
 */
@Slf4j
public class AgentBuilder {

    /**
     * Agent ID
     */
    private String agentId;

    /**
     * Agent 名称
     */
    private String name;

    /**
     * Agent 描述
     */
    private String description;

    /**
     * 版本号
     */
    private String versionNumber = "v1.0.0";

    /**
     * 版本描述
     */
    private String versionDescription;

    /**
     * 节点列表（临时存储）
     */
    private final Map<String, BaseNode> nodes = new HashMap<>();

    /**
     * 边列表（临时存储）
     */
    private final List<EdgeConfig> edges = new ArrayList<>();

    /**
     * 条件边列表（临时存储）
     */
    private final List<ConditionalEdgeConfig> conditionalEdges = new ArrayList<>();

    /**
     * 起始节点
     */
    private String startNode;

    /**
     * 结束节点
     */
    private String endNode;

    /**
     * 设置 Agent ID
     * @param agentId Agent ID
     * @return 当前 Builder 实例
     */
    public AgentBuilder agentId(String agentId) {
        this.agentId = agentId;
        return this;
    }

    /**
     * 设置 Agent 名称
     * @param name Agent 名称
     * @return 当前 Builder 实例
     */
    public AgentBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * 设置 Agent 描述
     * @param description Agent 描述
     * @return 当前 Builder 实例
     */
    public AgentBuilder description(String description) {
        this.description = description;
        return this;
    }

    /**
     * 设置版本号
     * @param versionNumber 版本号
     * @return 当前 Builder 实例
     */
    public AgentBuilder version(String versionNumber) {
        this.versionNumber = versionNumber;
        return this;
    }

    /**
     * 设置版本描述
     * @param versionDescription 版本描述
     * @return 当前 Builder 实例
     */
    public AgentBuilder versionDescription(String versionDescription) {
        this.versionDescription = versionDescription;
        return this;
    }

    /**
     * 添加节点
     * @param nodeId 节点 ID
     * @param node 节点实例
     * @return 当前 Builder 实例
     */
    public AgentBuilder addNode(String nodeId, BaseNode node) {
        this.nodes.put(nodeId, node);
        log.debug("添加节点：{}", nodeId);
        return this;
    }

    /**
     * 添加普通边
     * @param from 源节点 ID
     * @param to 目标节点 ID
     * @return 当前 Builder 实例
     */
    public AgentBuilder addEdge(String from, String to) {
        this.edges.add(new EdgeConfig(from, to));
        log.debug("添加边：{} -> {}", from, to);
        return this;
    }

    /**
     * 添加条件边
     * @param from 源节点 ID
     * @param condition 条件函数
     * @param mappings 条件映射（条件结果 -> 目标节点 ID）
     * @return 当前 Builder 实例
     */
    public AgentBuilder addConditionalEdge(String from, 
                                          Function<Map<String, Object>, String> condition,
                                          Map<String, String> mappings) {
        this.conditionalEdges.add(new ConditionalEdgeConfig(from, condition, mappings));
        log.debug("添加条件边：{}, mappings={}", from, mappings);
        return this;
    }

    /**
     * 添加条件边（简化的谓语形式）
     * @param from 源节点 ID
     * @param defaultTarget 默认目标节点
     * @param conditions 条件列表（谓语 -> 目标节点）
     * @return 当前 Builder 实例
     */
    public AgentBuilder addConditionalEdge(String from,
                                           String defaultTarget,
                                           Map<Predicate<Map<String, Object>>, String> conditions) {
        List<ConditionEdge.PredicateCondition> conditionList = conditions.entrySet().stream()
                .map(e -> new ConditionEdge.PredicateCondition(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        Map<String, String> mappings = new HashMap<>();
        for (String target : conditions.values()) {
            mappings.put(target, target);
        }

        ConditionEdge edge = ConditionEdge.builder()
                .from(from)
                .defaultTarget(defaultTarget)
                .predicateConditions(conditionList)
                .nodeMappings(mappings)
                .build();
        
        this.conditionalEdges.add(new ConditionalEdgeConfig(from, edge));
        log.debug("添加条件边（谓语形式）：{}, defaultTarget={}", from, defaultTarget);
        return this;
    }

    /**
     * 设置起始节点
     * @param nodeId 节点 ID
     * @return 当前 Builder 实例
     */
    public AgentBuilder setStartNode(String nodeId) {
        this.startNode = nodeId;
        return this;
    }

    /**
     * 设置结束节点
     * @param nodeId 节点 ID
     * @return 当前 Builder 实例
     */
    public AgentBuilder setEndNode(String nodeId) {
        this.endNode = nodeId;
        return this;
    }

    /**
     * 构建并注册 Agent
     * @param agentService AgentService 实例
     * @return 注册后的 AgentDefinition
     */
    public AgentDefinition buildAndRegister(AgentService agentService) {
        log.info("开始构建并注册 Agent：agentId={}", agentId);
        
        try {
            // 验证必填参数
            validate();
            
            // 构建 Graph
            Graph graph = buildGraph();
            
            // 构建 AgentDefinition
            AgentDefinition definition = buildAgentDefinition(graph);
            
            // 注册 Agent
            agentService.registerAgent(definition);
            
            log.info("Agent 构建并注册成功：agentId={}, name={}", agentId, name);
            return definition;
            
        } catch (Exception e) {
            log.error("Agent 构建失败：agentId={}", agentId, e);
            throw new RuntimeException("Agent 构建失败：" + e.getMessage(), e);
        }
    }

    /**
     * 仅构建 AgentDefinition，不注册
     * @return AgentDefinition
     */
    public AgentDefinition build() {
        log.info("开始构建 Agent：agentId={}", agentId);
        
        try {
            // 验证必填参数
            validate();
            
            // 构建 Graph
            Graph graph = buildGraph();
            
            // 构建 AgentDefinition
            return buildAgentDefinition(graph);
            
        } catch (Exception e) {
            log.error("Agent 构建失败：agentId={}", agentId, e);
            throw new RuntimeException("Agent 构建失败：" + e.getMessage(), e);
        }
    }

    /**
     * 验证必填参数
     */
    private void validate() {
        if (agentId == null || agentId.trim().isEmpty()) {
            throw new IllegalArgumentException("agentId 不能为空");
        }
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name 不能为空");
        }
        
        if (startNode == null || startNode.trim().isEmpty()) {
            throw new IllegalArgumentException("startNode 不能为空");
        }
        
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("至少需要添加一个节点");
        }
    }

    /**
     * 构建 Graph
     * @return Graph 实例
     */
    private Graph buildGraph() {
        Graph graph = new Graph();
        graph.setName(agentId + "_graph");
        graph.setDescription(description != null ? description : "");
        graph.setStartNode(startNode);
        graph.setEndNode(endNode != null ? endNode : "");
        
        // 添加节点
        graph.addAllNodes(nodes);
        
        // 添加边
        for (EdgeConfig edge : edges) {
            graph.addEdge(edge.from, edge.to);
        }
        
        // 添加条件边
        for (ConditionalEdgeConfig edge : conditionalEdges) {
            if (edge.conditionEdge != null) {
                graph.addConditionalEdge(edge.from, edge.conditionEdge);
            } else if (edge.condition != null && edge.mappings != null) {
                graph.addConditionalEdge(edge.from, edge.condition, edge.mappings);
            }
        }
        
        // 验证 Graph
        graph.validate();
        
        return graph;
    }

    /**
     * 构建 AgentDefinition
     * @param graph Graph 实例
     * @return AgentDefinition
     */
    private AgentDefinition buildAgentDefinition(Graph graph) {
        // 构建 AgentVersion
        AgentVersion version = AgentVersion.builder()
                .versionNumber(versionNumber)
                .description(versionDescription)
                .graph(graph)
                .createdAt(System.currentTimeMillis())
                .build();
        
        // 构建 AgentDefinition
        return AgentDefinition.builder()
                .agentId(agentId)
                .name(name)
                .description(description)
                .versions(new HashMap<>(Map.of(version.getVersionNumber(), version)))
                .currentVersion(versionNumber)
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();
    }

    /**
     * 边配置
     */
    private static class EdgeConfig {
        String from;
        String to;
        
        EdgeConfig(String from, String to) {
            this.from = from;
            this.to = to;
        }
    }

    /**
     * 条件边配置
     */
    private static class ConditionalEdgeConfig {
        String from;
        Function<Map<String, Object>, String> condition;
        Map<String, String> mappings;
        ConditionEdge conditionEdge;
        
        ConditionalEdgeConfig(String from, 
                            Function<Map<String, Object>, String> condition,
                            Map<String, String> mappings) {
            this.from = from;
            this.condition = condition;
            this.mappings = mappings;
        }
        
        ConditionalEdgeConfig(String from, ConditionEdge conditionEdge) {
            this.from = from;
            this.conditionEdge = conditionEdge;
        }
    }
}
