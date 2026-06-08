package com.shiyu.ai.agent.biz.agent.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiyu.ai.agent.biz.agent.domain.AgentDefinition;
import com.shiyu.ai.agent.biz.agent.domain.AgentVersion;
import com.shiyu.ai.agent.biz.agent.repository.AgentAdminRepository;
import com.shiyu.ai.agent.dal.dataobject.agent.AgentDefDO;
import com.shiyu.ai.agent.dal.dataobject.agent.AgentVersionDO;
import com.shiyu.ai.agent.domain.request.GraphConfigRequest;
import com.shiyu.ai.agent.langgraph4j.graph.ConditionEdge;
import com.shiyu.ai.agent.langgraph4j.graph.Graph;
import com.shiyu.ai.agent.langgraph4j.node.BaseNode;
import com.shiyu.ai.agent.langgraph4j.node.NodeConfig;
import com.shiyu.ai.agent.langgraph4j.node.NodeFactory;
import com.shiyu.ai.agent.langgraph4j.node.NodeType;
import com.shiyu.ai.agent.langgraph4j.node.agent.AgentCallConfig;
import com.shiyu.ai.agent.langgraph4j.node.condition.ConditionConfig;
import com.shiyu.ai.agent.langgraph4j.node.intent.IntentConfig;
import com.shiyu.ai.agent.langgraph4j.node.llm.LlmCallConfig;
import com.shiyu.ai.agent.langgraph4j.node.memory.LongTermMemoryConfig;
import com.shiyu.ai.agent.langgraph4j.node.memory.MemoryRetrievalConfig;
import com.shiyu.ai.agent.langgraph4j.node.memory.ShortTermMemoryConfig;
import com.shiyu.ai.agent.langgraph4j.node.output.OutputFormatConfig;
import com.shiyu.ai.agent.langgraph4j.node.rag.RagEnhancementConfig;
import com.shiyu.ai.agent.langgraph4j.node.rag.RagRetrievalConfig;
import com.shiyu.ai.agent.langgraph4j.node.tool.ToolCallConfig;
import com.shiyu.ai.agent.langgraph4j.node.transform.TransformConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.*;

@Slf4j
@Component
public class AgentLoader {

    private final ObjectMapper objectMapper;
    private final NodeFactory nodeFactory;
    private final AgentAdminRepository agentAdminRepository;

    public AgentLoader(NodeFactory nodeFactory, AgentAdminRepository agentAdminRepository) {
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.nodeFactory = nodeFactory;
        this.agentAdminRepository = agentAdminRepository;
    }

    public AgentDefinition loadFromDb(Long userId, String agentId) {
        log.info("从数据库加载 Agent: agentId={}", agentId);

        AgentDefDO agentDef = agentAdminRepository.selectByAgentId(agentId);
        if (agentDef == null || !"1".equals(agentDef.getStatus())) {
            log.warn("Agent 不存在或已停用: agentId={}", agentId);
            return null;
        }

        String versionNumber = agentDef.getCurrentVersion();
        if (versionNumber == null || versionNumber.isEmpty()) {
            log.warn("Agent 没有当前版本: agentId={}", agentId);
            return null;
        }

        AgentVersionDO versionDO = agentAdminRepository.selectVersionByAgentIdAndNumber(agentId, versionNumber);
        if (versionDO == null) {
            log.warn("Agent 当前版本不存在: agentId={}, version={}", agentId, versionNumber);
            return null;
        }

        if (versionDO.getGraphConfig() == null || versionDO.getGraphConfig().isEmpty()) {
            log.warn("Agent 版本无 Graph 配置: agentId={}, version={}", agentId, versionNumber);
            return null;
        }

        try {
            GraphConfigRequest graphConfig = objectMapper.readValue(versionDO.getGraphConfig(),
                    new TypeReference<GraphConfigRequest>() {});

            Graph graph = buildGraph(agentId, graphConfig);

            AgentVersion agentVersion = AgentVersion.builder()
                    .versionNumber(versionNumber)
                    .description(versionDO.getDescription())
                    .graph(graph)
                    .createdAt(versionDO.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .build();

            return AgentDefinition.builder()
                    .agentId(agentId)
                    .name(agentDef.getName())
                    .description(agentDef.getDescription())
                    .versions(new ArrayList<>(List.of(agentVersion)))
                    .currentVersion(versionNumber)
                    .createdAt(agentDef.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .updatedAt(agentDef.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .build();

        } catch (Exception e) {
            log.error("从数据库加载 Agent 失败: agentId={}", agentId, e);
            return null;
        }
    }

    public Graph buildGraph(String agentId, GraphConfigRequest graphConfig) {
        String graphName = graphConfig.getName() != null ? graphConfig.getName() : agentId + "_graph";
        Graph graph = new Graph();
        graph.setName(graphName);
        graph.setDescription(graphConfig.getDescription() != null ? graphConfig.getDescription() : "");
        graph.setStartNode(graphConfig.getStartNode() != null ? graphConfig.getStartNode() : "");
        graph.setEndNode(graphConfig.getEndNode() != null ? graphConfig.getEndNode() : "");

        Map<String, BaseNode> nodes = new HashMap<>();
        if (graphConfig.getNodes() != null) {
            for (Map.Entry<String, GraphConfigRequest.NodeConfigDTO> entry : graphConfig.getNodes().entrySet()) {
                String nodeId = entry.getKey();
                GraphConfigRequest.NodeConfigDTO dto = entry.getValue();
                try {
                    NodeConfig config = buildNodeConfig(nodeId, dto);
                    BaseNode node = nodeFactory.createNode(config);
                    nodes.put(nodeId, node);
                } catch (Exception e) {
                    log.error("创建节点失败: nodeId={}", nodeId, e);
                    throw new RuntimeException("创建节点失败: " + nodeId, e);
                }
            }
        }
        graph.addAllNodes(nodes);

        if (graphConfig.getEdges() != null) {
            for (Map.Entry<String, List<String>> entry : graphConfig.getEdges().entrySet()) {
                for (String targetId : entry.getValue()) {
                    graph.addEdge(entry.getKey(), targetId);
                }
            }
        }

        if (graphConfig.getConditionalEdges() != null) {
            for (Map.Entry<String, GraphConfigRequest.ConditionalEdgeDTO> entry : graphConfig.getConditionalEdges().entrySet()) {
                String sourceId = entry.getKey();
                GraphConfigRequest.ConditionalEdgeDTO edgeDto = entry.getValue();
                ConditionEdge conditionEdge = buildConditionEdge(sourceId, edgeDto);
                graph.addConditionalEdge(sourceId, conditionEdge);
            }
        }

        graph.validate();
        log.info("Graph 构建并验证成功: {}", graphName);
        return graph;
    }

    private NodeConfig buildNodeConfig(String nodeId, GraphConfigRequest.NodeConfigDTO dto) {
        NodeType nodeType = NodeType.fromCode(dto.getNodeType());
        Class<? extends NodeConfig> configClass = getConfigClass(nodeType);

        Map<String, Object> merged = new LinkedHashMap<>();
        merged.put("nodeId", nodeId);
        merged.put("nodeName", dto.getNodeName() != null ? dto.getNodeName() : nodeId);
        merged.put("description", dto.getDescription() != null ? dto.getDescription() : "");
        merged.put("nodeType", dto.getNodeType());
        merged.put("enabled", dto.getEnabled() != null ? dto.getEnabled() : true);
        merged.put("timeout", dto.getTimeout() != null ? dto.getTimeout() : 30000L);
        merged.put("retryCount", dto.getRetryCount() != null ? dto.getRetryCount() : 0);
        merged.put("retryInterval", dto.getRetryInterval() != null ? dto.getRetryInterval() : 1000L);
        merged.put("errorStrategy", dto.getErrorStrategy() != null ? dto.getErrorStrategy() : "THROW");
        merged.put("logLevel", dto.getLogLevel() != null ? dto.getLogLevel() : "INFO");
        merged.put("properties", dto.getProperties() != null ? dto.getProperties() : new HashMap<>());

        if (dto.getConfig() != null) {
            merged.putAll(dto.getConfig());
        }

        return objectMapper.convertValue(merged, configClass);
    }

    private ConditionEdge buildConditionEdge(String sourceId, GraphConfigRequest.ConditionalEdgeDTO dto) {
        Map<String, String> mappings = dto.getNodeMappings() != null ? dto.getNodeMappings() : new HashMap<>();
        String defaultTarget = dto.getDefaultTarget() != null ? dto.getDefaultTarget() : "";

        return ConditionEdge.builder()
                .from(sourceId)
                .defaultTarget(defaultTarget)
                .nodeMappings(mappings)
                .functionCondition(state -> {
                    String intentCode = (String) state.getOrDefault("intentCode", "");
                    return mappings.getOrDefault(intentCode, defaultTarget);
                })
                .build();
    }

    private Class<? extends NodeConfig> getConfigClass(NodeType nodeType) {
        return switch (nodeType) {
            case INTENT -> IntentConfig.class;
            case LLM_CALL -> LlmCallConfig.class;
            case RAG_RETRIEVAL -> RagRetrievalConfig.class;
            case RAG_ENHANCEMENT -> RagEnhancementConfig.class;
            case TOOL_CALL -> ToolCallConfig.class;
            case CONDITION -> ConditionConfig.class;
            case TRANSFORM -> TransformConfig.class;
            case OUTPUT_FORMAT -> OutputFormatConfig.class;
            case MEMORY_SHORT_TERM -> ShortTermMemoryConfig.class;
            case MEMORY_LONG_TERM -> LongTermMemoryConfig.class;
            case MEMORY_RETRIEVAL -> MemoryRetrievalConfig.class;
            case AGENT_CALL -> AgentCallConfig.class;
            default -> NodeConfig.class;
        };
    }
}
