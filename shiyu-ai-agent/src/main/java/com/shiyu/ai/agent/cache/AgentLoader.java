package com.shiyu.ai.agent.cache;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.AgentVersion;
import com.shiyu.ai.dal.agent.repository.AgentAdminRepository;
import com.shiyu.ai.dal.agent.bo.AgentDefBO;
import com.shiyu.ai.dal.agent.bo.AgentVersionBO;
import com.shiyu.ai.agent.request.GraphConfigRequest;
import com.shiyu.ai.agent.graph.ConditionEdge;
import com.shiyu.ai.agent.graph.Graph;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeFactory;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.agent.AgentCallConfig;
import com.shiyu.ai.agent.node.condition.ConditionConfig;
import com.shiyu.ai.agent.node.intent.IntentConfig;
import com.shiyu.ai.agent.node.llm.LlmCallConfig;
import com.shiyu.ai.agent.node.memory.LongTermMemoryConfig;
import com.shiyu.ai.agent.node.memory.MemoryRetrievalConfig;
import com.shiyu.ai.agent.node.memory.ShortTermMemoryConfig;
import com.shiyu.ai.agent.node.output.OutputFormatConfig;
import com.shiyu.ai.agent.node.rag.RagEnhancementConfig;
import com.shiyu.ai.agent.node.rag.RagRetrievalConfig;
import com.shiyu.ai.agent.node.tool.ToolCallConfig;
import com.shiyu.ai.agent.node.transform.TransformConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.*;

@Slf4j
@Component
public class AgentLoader {

    private final NodeFactory nodeFactory;
    private final AgentAdminRepository agentAdminRepository;

    public AgentLoader(NodeFactory nodeFactory, AgentAdminRepository agentAdminRepository) {
        this.nodeFactory = nodeFactory;
        this.agentAdminRepository = agentAdminRepository;
    }

    public AgentDefinition loadFromDb(Long userId, String agentId) {
        log.info("从数据库加载 Agent: agentId={}", agentId);

        AgentDefBO agentDef = agentAdminRepository.selectByAgentId(agentId);
        if (agentDef == null || agentDef.getStatus() == null || agentDef.getStatus() != 1) {
            log.warn("Agent 不存在或已停用: agentId={}", agentId);
            return null;
        }

        String versionNumber = agentDef.getCurrentVersion();
        if (versionNumber == null || versionNumber.isEmpty()) {
            log.warn("Agent 没有当前版本: agentId={}", agentId);
            return null;
        }

        AgentVersionBO versionBO = agentAdminRepository.selectVersionByAgentIdAndNumber(agentId, versionNumber);
        if (versionBO == null) {
            log.warn("Agent 当前版本不存在: agentId={}, version={}", agentId, versionNumber);
            return null;
        }

        if (versionBO.getGraphConfig() == null || versionBO.getGraphConfig().isEmpty()) {
            log.warn("Agent 版本无 Graph 配置: agentId={}, version={}", agentId, versionNumber);
            return null;
        }

        try {
            GraphConfigRequest graphConfig = JSONUtils.parseObject(versionBO.getGraphConfig(),
                    new tools.jackson.core.type.TypeReference<GraphConfigRequest>() {});

            Graph graph = buildGraph(agentId, graphConfig);

            // ===== 提取节点入参定义 → ext_info.requiredInputs =====
            try {
                java.util.List<com.shiyu.ai.agent.node.NodeInputParam> allInputs = new java.util.ArrayList<>();
                for (Map.Entry<String, com.shiyu.ai.agent.node.BaseNode> entry : graph.getNodes().entrySet()) {
                    String nodeId = entry.getKey();
                    com.shiyu.ai.agent.node.BaseNode node = entry.getValue();
                    java.util.List<com.shiyu.ai.agent.node.NodeInputParam> nodeInputs = node.getRequiredInputs();
                    for (com.shiyu.ai.agent.node.NodeInputParam p : nodeInputs) {
                        allInputs.add(new com.shiyu.ai.agent.node.NodeInputParam(
                                p.name(), p.type(), p.source(), p.required(),
                                "[" + nodeId + "] " + p.description(), p.defaultValue()
                        ));
                    }
                }
                // 去重
                java.util.LinkedHashMap<String, com.shiyu.ai.agent.node.NodeInputParam> deduped = new java.util.LinkedHashMap<>();
                for (com.shiyu.ai.agent.node.NodeInputParam p : allInputs) {
                    String key = p.name() + "|" + p.source().name();
                    if (!deduped.containsKey(key)) {
                        deduped.put(key, p);
                    }
                }
                java.util.Map<String, Object> extInfoMap = new java.util.HashMap<>();
                extInfoMap.put("requiredInputs", deduped.values());
                String extInfoJson = com.shiyu.ai.common.core.utils.JSONUtils.toJsonString(extInfoMap);
                
                if (!extInfoJson.equals(versionBO.getExtInfo())) {
                    versionBO.setExtInfo(extInfoJson);
                    agentAdminRepository.updateVersion(versionBO);
                    log.info("agent_version.ext_info 已更新: agentId={}, version={}", agentId, versionNumber);
                }
                if (!extInfoJson.equals(agentDef.getExtInfo())) {
                    agentDef.setExtInfo(extInfoJson);
                    agentAdminRepository.update(agentDef);
                    log.info("agent_def.ext_info 已同步: agentId={}", agentId);
                }
            } catch (Exception e) {
                log.warn("提取节点入参定义失败（不影响执行）: agentId={}", agentId, e);
            }
            // ===== end ext_info =====

            AgentVersion agentVersion = AgentVersion.builder()
                    .versionNumber(versionNumber)
                    .description(versionBO.getDescription())
                    .graph(graph)
                    .createdAt(versionBO.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .build();

            return AgentDefinition.builder()
                    .agentId(agentId)
                    .name(agentDef.getName())
                    .description(agentDef.getDescription())
                    .extInfo(agentDef.getExtInfo() != null ? JSONUtils.parseObject(agentDef.getExtInfo(), new tools.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {}) : null)
                    .versions(new HashMap<>(Map.of(agentVersion.getVersionNumber(), agentVersion)))
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

        return JSONUtils.convertValue(merged, configClass);
    }

    private ConditionEdge buildConditionEdge(String sourceId, GraphConfigRequest.ConditionalEdgeDTO dto) {
        Map<String, String> mappings = dto.getNodeMappings() != null ? dto.getNodeMappings() : new HashMap<>();
        String defaultTarget = dto.getDefaultTarget() != null ? dto.getDefaultTarget() : "";
        String conditionType = dto.getConditionType() != null ? dto.getConditionType() : "";

        return ConditionEdge.builder()
                .from(sourceId)
                .defaultTarget(defaultTarget)
                .nodeMappings(mappings)
                .functionCondition(state -> {
                    if ("SCORE_ROUTING".equals(conditionType)) {
                        // 教育图评分路由：reviewNeeded=true → retry, false → pass
                        Boolean reviewNeeded = (Boolean) state.get("reviewNeeded");
                        return Boolean.TRUE.equals(reviewNeeded) ? "retry" : "pass";
                    }
                    // 默认：意图路由
                    String intentCode = (String) state.getOrDefault("intentCode", "");
                    return intentCode.isEmpty() ? "UNKNOWN" : intentCode;
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
