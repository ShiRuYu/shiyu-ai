package com.shiyu.ai.agent.biz.agent.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiyu.ai.agent.biz.agent.cache.AgentCacheManager;
import com.shiyu.ai.agent.biz.agent.repository.AgentAdminRepository;
import com.shiyu.ai.agent.biz.agent.service.AgentAdminService;
import com.shiyu.ai.agent.biz.agent.service.AgentService;
import com.shiyu.ai.agent.dal.dataobject.agent.AgentDefDO;
import com.shiyu.ai.agent.dal.dataobject.agent.AgentVersionDO;
import com.shiyu.ai.agent.domain.request.*;
import com.shiyu.ai.agent.domain.vo.*;
import com.shiyu.ai.agent.langgraph4j.graph.Graph;
import com.shiyu.ai.agent.langgraph4j.node.NodeType;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AgentAdminServiceImpl implements AgentAdminService {

    @Resource
    private AgentAdminRepository agentAdminRepository;

    @Resource
    private AgentCacheManager cacheManager;

    @Resource
    private AgentService agentService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public Pair<Long, List<AgentVO>> getPage(Number pageNo, Number pageSize, String name, String status) {
        Pair<Long, List<AgentDefDO>> result = agentAdminRepository.selectPage(pageNo, pageSize, name, status);
        List<AgentVO> vos = result.getRight().stream().map(this::toVO).collect(Collectors.toList());
        return Pair.of(result.getLeft(), vos);
    }

    @Override
    public AgentDetailVO getById(Long id) {
        AgentDefDO def = agentAdminRepository.selectById(id);
        if (def == null) return null;
        List<AgentVersionDO> versions = agentAdminRepository.selectVersionsByAgentId(def.getAgentId());
        List<AgentVersionVO> versionVOs = versions.stream().map(this::toVersionVO).collect(Collectors.toList());
        return AgentDetailVO.builder()
                .id(def.getId()).agentId(def.getAgentId()).name(def.getName())
                .description(def.getDescription()).currentVersion(def.getCurrentVersion())
                .status(def.getStatus()).versions(versionVOs)
                .createTime(def.getCreateTime()).updateTime(def.getUpdateTime())
                .build();
    }

    @Override
    public AgentVO create(AgentRequest request) {
        AgentDefDO existing = agentAdminRepository.selectByAgentId(request.getAgentId());
        if (existing != null) {
            throw new IllegalArgumentException("Agent标识已存在: " + request.getAgentId());
        }
        AgentDefDO def = new AgentDefDO();
        def.setAgentId(request.getAgentId());
        def.setName(request.getName());
        def.setDescription(request.getDescription());
        def.setStatus(request.getStatus() != null ? request.getStatus() : "1");
        agentAdminRepository.create(def);
        return toVO(def);
    }

    @Override
    public AgentVO update(Long id, AgentRequest request) {
        AgentDefDO def = agentAdminRepository.selectById(id);
        if (def == null) throw new IllegalArgumentException("Agent不存在: " + id);
        if (request.getName() != null) def.setName(request.getName());
        if (request.getDescription() != null) def.setDescription(request.getDescription());
        if (request.getStatus() != null) def.setStatus(request.getStatus());
        def.setUpdateTime(LocalDateTime.now());
        agentAdminRepository.update(def);
        evictAgentCache(def.getAgentId());
        return toVO(def);
    }

    @Override
    public void deleteById(Long id) {
        AgentDefDO def = agentAdminRepository.selectById(id);
        if (def == null) return;
        agentAdminRepository.deleteById(id);
        evictAgentCache(def.getAgentId());
    }

    @Override
    public List<AgentVersionVO> getVersions(String agentId) {
        List<AgentVersionDO> versions = agentAdminRepository.selectVersionsByAgentId(agentId);
        return versions.stream().map(this::toVersionVO).collect(Collectors.toList());
    }

    @Override
    public AgentVersionDetailVO getVersionDetail(String agentId, Long versionId) {
        AgentVersionDO v = agentAdminRepository.selectVersionById(versionId);
        if (v == null || !v.getAgentId().equals(agentId)) return null;
        return toVersionDetailVO(v);
    }

    @Override
    public AgentVersionVO createVersion(String agentId, VersionRequest request) {
        AgentDefDO def = agentAdminRepository.selectByAgentId(agentId);
        if (def == null) throw new IllegalArgumentException("Agent不存在: " + agentId);

        AgentVersionDO existing = agentAdminRepository.selectVersionByAgentIdAndNumber(agentId, request.getVersionNumber());
        if (existing != null) throw new IllegalArgumentException("版本号已存在: " + request.getVersionNumber());

        AgentVersionDO version = new AgentVersionDO();
        version.setAgentId(agentId);
        version.setVersionNumber(request.getVersionNumber());
        version.setDescription(request.getDescription());
        version.setStatus("DRAFT");

        if (request.getCopyFromVersionId() != null) {
            AgentVersionDO source = agentAdminRepository.selectVersionById(request.getCopyFromVersionId());
            if (source != null) {
                version.setGraphConfig(source.getGraphConfig());
                version.setCanvasConfig(source.getCanvasConfig());
            }
        }

        agentAdminRepository.createVersion(version);
        evictAgentCache(agentId);
        return toVersionVO(version);
    }

    @Override
    public AgentVersionVO updateVersion(String agentId, Long versionId, VersionRequest request) {
        AgentVersionDO v = agentAdminRepository.selectVersionById(versionId);
        if (v == null || !v.getAgentId().equals(agentId)) throw new IllegalArgumentException("版本不存在: " + versionId);
        if (request.getDescription() != null) v.setDescription(request.getDescription());
        v.setUpdateTime(LocalDateTime.now());
        agentAdminRepository.updateVersion(v);
        return toVersionVO(v);
    }

    @Override
    public void deleteVersion(String agentId, Long versionId) {
        AgentVersionDO v = agentAdminRepository.selectVersionById(versionId);
        if (v == null || !v.getAgentId().equals(agentId)) return;
        agentAdminRepository.deleteVersionById(versionId);
        evictAgentCache(agentId);
    }

    @Override
    public void publishVersion(String agentId, Long versionId) {
        AgentVersionDO v = agentAdminRepository.selectVersionById(versionId);
        if (v == null || !v.getAgentId().equals(agentId)) throw new IllegalArgumentException("版本不存在");
        if (!"DRAFT".equals(v.getStatus())) throw new IllegalArgumentException("只有草稿状态才能发布");
        v.setStatus("PUBLISHED");
        v.setUpdateTime(LocalDateTime.now());
        agentAdminRepository.updateVersion(v);
        evictAgentCache(agentId);
    }

    @Override
    public void archiveVersion(String agentId, Long versionId) {
        AgentVersionDO v = agentAdminRepository.selectVersionById(versionId);
        if (v == null || !v.getAgentId().equals(agentId)) throw new IllegalArgumentException("版本不存在");
        if ("ARCHIVED".equals(v.getStatus())) throw new IllegalArgumentException("版本已归档");
        v.setStatus("ARCHIVED");
        v.setUpdateTime(LocalDateTime.now());
        agentAdminRepository.updateVersion(v);
        evictAgentCache(agentId);
    }

    @Override
    public void activateVersion(String agentId, Long versionId) {
        AgentVersionDO v = agentAdminRepository.selectVersionById(versionId);
        if (v == null || !v.getAgentId().equals(agentId)) throw new IllegalArgumentException("版本不存在");
        if (!"PUBLISHED".equals(v.getStatus())) throw new IllegalArgumentException("只有已发布版本才能激活");

        AgentDefDO def = agentAdminRepository.selectByAgentId(agentId);
        if (def == null) throw new IllegalArgumentException("Agent不存在");
        def.setCurrentVersion(v.getVersionNumber());
        def.setUpdateTime(LocalDateTime.now());
        agentAdminRepository.update(def);

        evictAgentCache(agentId);
    }

    @Override
    public AgentVersionVO copyVersion(String agentId, VersionRequest request) {
        return createVersion(agentId, request);
    }

    @Override
    public AgentVersionDetailVO getGraphConfig(String agentId, Long versionId) {
        AgentVersionDO v = agentAdminRepository.selectVersionById(versionId);
        if (v == null || !v.getAgentId().equals(agentId)) return null;
        return toVersionDetailVO(v);
    }

    @Override
    public AgentVersionDetailVO updateGraphConfig(String agentId, Long versionId, GraphConfigRequest request) {
        AgentVersionDO v = agentAdminRepository.selectVersionById(versionId);
        if (v == null || !v.getAgentId().equals(agentId)) throw new IllegalArgumentException("版本不存在");

        try {
            String json = objectMapper.writeValueAsString(request);
            v.setGraphConfig(json);
            v.setUpdateTime(LocalDateTime.now());
            agentAdminRepository.updateVersion(v);
            evictAgentCache(agentId);
        } catch (Exception e) {
            throw new RuntimeException("序列化Graph配置失败", e);
        }
        return toVersionDetailVO(v);
    }

    @Override
    public GraphValidationVO validateGraphConfig(GraphConfigRequest request) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        try {
            Graph graph = new Graph();
            graph.setName(request.getName() != null ? request.getName() : "temp");
            graph.setStartNode(request.getStartNode() != null ? request.getStartNode() : "");
            graph.setEndNode(request.getEndNode() != null ? request.getEndNode() : "");

            if (request.getNodes() != null) {
                for (String nodeId : request.getNodes().keySet()) {
                    if (!nodeId.equals(graph.getStartNode()) && !graph.getStartNode().isEmpty()) {
                        boolean hasIncoming = false;
                        if (request.getEdges() != null) {
                            for (List<String> targets : request.getEdges().values()) {
                                if (targets.contains(nodeId)) { hasIncoming = true; break; }
                            }
                        }
                        if (request.getConditionalEdges() != null) {
                            for (GraphConfigRequest.ConditionalEdgeDTO ce : request.getConditionalEdges().values()) {
                                if (ce.getNodeMappings() != null && ce.getNodeMappings().values().contains(nodeId)) {
                                    warnings.add("节点 " + nodeId + " 通过条件边可达");
                                }
                            }
                        }
                    }
                }
            }
            return GraphValidationVO.success();
        } catch (Exception e) {
            errors.add(e.getMessage());
            return GraphValidationVO.fail(errors, warnings);
        }
    }

    @Override
    public void addNode(String agentId, Long versionId, NodeConfigRequest request) {
        AgentVersionDO v = getVersionOrThrow(agentId, versionId);
        Map<String, Object> graphData = parseGraphConfig(v.getGraphConfig());
        Map<String, Object> nodes = getMap(graphData, "nodes");
        Map<String, Object> nodeConfig = new LinkedHashMap<>();
        nodeConfig.put("nodeName", request.getNodeName());
        nodeConfig.put("description", request.getDescription() != null ? request.getDescription() : "");
        nodeConfig.put("nodeType", request.getNodeType());
        nodeConfig.put("enabled", request.getEnabled() != null ? request.getEnabled() : true);
        nodeConfig.put("timeout", request.getTimeout() != null ? request.getTimeout() : 30000L);
        nodeConfig.put("retryCount", request.getRetryCount() != null ? request.getRetryCount() : 0);
        nodeConfig.put("retryInterval", request.getRetryInterval() != null ? request.getRetryInterval() : 1000L);
        nodeConfig.put("errorStrategy", request.getErrorStrategy() != null ? request.getErrorStrategy() : "THROW");
        nodeConfig.put("config", request.getConfig() != null ? request.getConfig() : new HashMap<>());
        nodes.put(request.getNodeId(), nodeConfig);
        saveGraphConfig(v, graphData);
    }

    @Override
    public void updateNode(String agentId, Long versionId, String nodeId, NodeConfigRequest request) {
        AgentVersionDO v = getVersionOrThrow(agentId, versionId);
        Map<String, Object> graphData = parseGraphConfig(v.getGraphConfig());
        Map<String, Object> nodes = getMap(graphData, "nodes");
        if (!nodes.containsKey(nodeId)) throw new IllegalArgumentException("节点不存在: " + nodeId);

        Map<String, Object> nodeConfig = getMap(nodes, nodeId);
        if (request.getNodeName() != null) nodeConfig.put("nodeName", request.getNodeName());
        if (request.getDescription() != null) nodeConfig.put("description", request.getDescription());
        if (request.getNodeType() != null) nodeConfig.put("nodeType", request.getNodeType());
        if (request.getEnabled() != null) nodeConfig.put("enabled", request.getEnabled());
        if (request.getTimeout() != null) nodeConfig.put("timeout", request.getTimeout());
        if (request.getRetryCount() != null) nodeConfig.put("retryCount", request.getRetryCount());
        if (request.getErrorStrategy() != null) nodeConfig.put("errorStrategy", request.getErrorStrategy());
        if (request.getConfig() != null) nodeConfig.put("config", request.getConfig());

        saveGraphConfig(v, graphData);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void deleteNode(String agentId, Long versionId, String nodeId) {
        AgentVersionDO v = getVersionOrThrow(agentId, versionId);
        Map<String, Object> graphData = parseGraphConfig(v.getGraphConfig());
        Map<String, Object> nodes = getMap(graphData, "nodes");
        nodes.remove(nodeId);

        Map<String, Object> edges = getMap(graphData, "edges");
        edges.remove(nodeId);
        for (Object value : edges.values()) {
            if (value instanceof List) {
                ((List<String>) value).remove(nodeId);
            }
        }

        Map<String, Object> conditionalEdges = getMap(graphData, "conditionalEdges");
        conditionalEdges.remove(nodeId);

        if (nodeId.equals(graphData.get("startNode"))) graphData.remove("startNode");
        if (nodeId.equals(graphData.get("endNode"))) graphData.remove("endNode");

        saveGraphConfig(v, graphData);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addEdge(String agentId, Long versionId, EdgeRequest request) {
        AgentVersionDO v = getVersionOrThrow(agentId, versionId);
        Map<String, Object> graphData = parseGraphConfig(v.getGraphConfig());

        if (request.getConditionType() != null) {
            Map<String, Object> conditionalEdges = getMap(graphData, "conditionalEdges");
            Map<String, Object> condConfig = new LinkedHashMap<>();
            condConfig.put("defaultTarget", request.getDefaultTarget());
            condConfig.put("nodeMappings", request.getConditionMappings() != null ? request.getConditionMappings() : new HashMap<>());
            condConfig.put("conditionType", request.getConditionType());
            conditionalEdges.put(request.getSourceNodeId(), condConfig);
        } else {
            Map<String, Object> edges = getMap(graphData, "edges");
            List<String> targets = (List<String>) edges.computeIfAbsent(request.getSourceNodeId(), k -> new ArrayList<>());
            if (!targets.contains(request.getTargetNodeId())) {
                targets.add(request.getTargetNodeId());
            }
        }

        saveGraphConfig(v, graphData);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void deleteEdge(String agentId, Long versionId, String sourceNodeId, String targetNodeId) {
        AgentVersionDO v = getVersionOrThrow(agentId, versionId);
        Map<String, Object> graphData = parseGraphConfig(v.getGraphConfig());

        Map<String, Object> edges = getMap(graphData, "edges");
        Object targets = edges.get(sourceNodeId);
        if (targets instanceof List) {
            ((List<String>) targets).remove(targetNodeId);
            if (((List<String>) targets).isEmpty()) {
                edges.remove(sourceNodeId);
            }
        }

        Map<String, Object> conditionalEdges = getMap(graphData, "conditionalEdges");
        if (conditionalEdges.containsKey(sourceNodeId)) {
            conditionalEdges.remove(sourceNodeId);
        }

        saveGraphConfig(v, graphData);
    }

    @Override
    public String getCanvasConfig(String agentId, Long versionId) {
        AgentVersionDO v = agentAdminRepository.selectVersionById(versionId);
        if (v == null || !v.getAgentId().equals(agentId)) return null;
        return v.getCanvasConfig();
    }

    @Override
    public void updateCanvasConfig(String agentId, Long versionId, String canvasConfig) {
        AgentVersionDO v = getVersionOrThrow(agentId, versionId);
        v.setCanvasConfig(canvasConfig);
        v.setUpdateTime(LocalDateTime.now());
        agentAdminRepository.updateVersion(v);
    }

    @Override
    public List<NodeTypeMetaVO> getNodeTypes() {
        List<NodeTypeMetaVO> metas = new ArrayList<>();
        for (NodeType type : NodeType.values()) {
            metas.add(NodeTypeMetaVO.builder()
                    .code(type.getCode())
                    .name(type.getName())
                    .description(type.getDescription())
                    .icon("icon-" + type.getCode().toLowerCase().replace("_", "-"))
                    .color(getNodeColor(type))
                    .fields(buildFieldMetas(type))
                    .build());
        }
        return metas;
    }

    @Override
    public List<IdNameOptionVO> listAllOptions() {
        List<AgentDefDO> list = agentAdminRepository.selectAllActive();
        return list.stream().map(d -> IdNameOptionVO.builder()
                .id(d.getId())
                .name(d.getName())
                .code(d.getAgentId())
                .build()).collect(Collectors.toList());
    }

    private AgentVersionDO getVersionOrThrow(String agentId, Long versionId) {
        AgentVersionDO v = agentAdminRepository.selectVersionById(versionId);
        if (v == null || !v.getAgentId().equals(agentId)) {
            throw new IllegalArgumentException("版本不存在: " + versionId);
        }
        return v;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseGraphConfig(String graphConfig) {
        if (graphConfig == null || graphConfig.isEmpty()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("nodes", new LinkedHashMap<>());
            empty.put("edges", new LinkedHashMap<>());
            empty.put("conditionalEdges", new LinkedHashMap<>());
            return empty;
        }
        try {
            return objectMapper.readValue(graphConfig, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("解析Graph配置失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Map<String, Object> parent, String key) {
        Object value = parent.computeIfAbsent(key, k -> new LinkedHashMap<>());
        return (Map<String, Object>) value;
    }

    private void saveGraphConfig(AgentVersionDO v, Map<String, Object> graphData) {
        try {
            v.setGraphConfig(objectMapper.writeValueAsString(graphData));
            v.setUpdateTime(LocalDateTime.now());
            agentAdminRepository.updateVersion(v);
            evictAgentCache(v.getAgentId());
        } catch (Exception e) {
            throw new RuntimeException("保存Graph配置失败", e);
        }
    }

    private void evictAgentCache(String agentId) {
        cacheManager.evictColumn(agentId);
        agentService.evictRuntimeCache(agentId);
    }

    private AgentVO toVO(AgentDefDO def) {
        return AgentVO.builder()
                .id(def.getId()).agentId(def.getAgentId()).name(def.getName())
                .description(def.getDescription()).currentVersion(def.getCurrentVersion())
                .status(def.getStatus())
                .createTime(def.getCreateTime()).updateTime(def.getUpdateTime())
                .build();
    }

    private AgentVersionVO toVersionVO(AgentVersionDO v) {
        return AgentVersionVO.builder()
                .id(v.getId()).agentId(v.getAgentId()).versionNumber(v.getVersionNumber())
                .description(v.getDescription()).status(v.getStatus())
                .createTime(v.getCreateTime()).updateTime(v.getUpdateTime())
                .build();
    }

    private AgentVersionDetailVO toVersionDetailVO(AgentVersionDO v) {
        AgentVersionDetailVO.GraphConfigVO graphVO = null;
        if (v.getGraphConfig() != null && !v.getGraphConfig().isEmpty()) {
            try {
                Map<String, Object> graphData = objectMapper.readValue(v.getGraphConfig(),
                        new TypeReference<Map<String, Object>>() {});
                graphVO = AgentVersionDetailVO.GraphConfigVO.builder()
                        .name((String) graphData.get("name"))
                        .description((String) graphData.get("description"))
                        .startNode((String) graphData.get("startNode"))
                        .endNode((String) graphData.get("endNode"))
                        .nodes(getMap(graphData, "nodes"))
                        .edges(getMap(graphData, "edges"))
                        .conditionalEdges(getMap(graphData, "conditionalEdges"))
                        .build();
            } catch (Exception ignored) {
            }
        }
        return AgentVersionDetailVO.builder()
                .id(v.getId()).agentId(v.getAgentId()).versionNumber(v.getVersionNumber())
                .description(v.getDescription()).status(v.getStatus())
                .graphConfig(graphVO).canvasConfig(v.getCanvasConfig())
                .createTime(v.getCreateTime()).updateTime(v.getUpdateTime())
                .build();
    }

    private String getNodeColor(NodeType type) {
        return switch (type) {
            case INTENT -> "#FF9800";
            case LLM_CALL -> "#4CAF50";
            case RAG_RETRIEVAL, RAG_ENHANCEMENT -> "#2196F3";
            case TOOL_CALL -> "#9C27B0";
            case CONDITION -> "#FF5722";
            case TRANSFORM -> "#607D8B";
            case OUTPUT_FORMAT -> "#00BCD4";
            case MEMORY_SHORT_TERM, MEMORY_LONG_TERM, MEMORY_RETRIEVAL -> "#795548";
            case AGENT_CALL -> "#E91E63";
            default -> "#757575";
        };
    }

    private List<NodeTypeMetaVO.FieldMeta> buildFieldMetas(NodeType type) {
        List<NodeTypeMetaVO.FieldMeta> fields = new ArrayList<>();
        switch (type) {
            case INTENT:
                fields.add(field("category", "意图分类", "text", "", false, "分类标识"));
                fields.add(field("confidenceThreshold", "置信度阈值", "number", 0.75, false, "意图识别的最低置信度"));
                fields.add(field("platform", "AI平台", "text", "", false, "LLM平台编码(如DEEPSEEK)"));
                fields.add(field("modelName", "模型名称", "text", "", false, "模型名称(如deepseek-chat)"));
                break;
            case LLM_CALL:
                fields.add(field("platform", "AI平台", "text", "", false, "LLM平台编码"));
                fields.add(field("modelName", "模型名称", "text", "", false, "模型名称"));
                fields.add(field("temperature", "温度参数", "number", 0.7, false, "控制输出随机性(0-2)"));
                fields.add(field("maxTokens", "最大Token数", "number", 4096, false, "输出最大长度"));
                fields.add(field("topP", "Top-P", "number", 0.9, false, "核采样参数"));
                fields.add(field("systemPrompt", "系统提示词", "textarea", "", false, "系统级别的指令"));
                fields.add(field("defaultPrompt", "默认提示词", "textarea", "", false, "默认的用户提示词"));
                fields.add(field("promptTemplate", "提示词模板", "textarea", "", false, "支持{context}{query}占位符"));
                fields.add(field("stream", "流式输出", "boolean", false, false, "是否使用流式输出"));
                break;
            case RAG_RETRIEVAL:
                fields.add(field("knowledgeBaseId", "知识库ID", "text", "", false, "目标知识库ID"));
                fields.add(field("topK", "检索数量", "number", 5, false, "返回的相关文档数量"));
                fields.add(field("similarityThreshold", "相似度阈值", "number", 0.7, false, "最低相似度"));
                fields.add(field("enableRerank", "重排序", "boolean", false, false, "是否启用重排序"));
                break;
            case RAG_ENHANCEMENT:
                fields.add(field("enhancementStrategy", "增强策略", "select", "SUMMARIZATION", false, Map.of("options", List.of("SUMMARIZATION", "CHUNKING", "HYBRID"))));
                fields.add(field("contextWindowSize", "上下文窗口", "number", 3, false, "上下文窗口大小"));
                fields.add(field("maxLength", "最大长度", "number", 2000, false, "输出最大长度"));
                fields.add(field("addContext", "添加上下文", "boolean", true, false, "是否添加上下文"));
                break;
            case TOOL_CALL:
                fields.add(field("toolName", "工具名称", "text", "", true, "调用的工具标识"));
                fields.add(field("toolType", "工具类型", "text", "", false, "工具类型分类"));
                fields.add(field("toolTimeout", "超时时间(ms)", "number", 10000L, false, "工具调用超时"));
                fields.add(field("enableCache", "启用缓存", "boolean", false, false, "是否缓存工具结果"));
                break;
            case CONDITION:
                fields.add(field("conditionExpression", "条件表达式", "textarea", "", false, "条件判断表达式"));
                fields.add(field("conditionType", "条件类型", "select", "EXPRESSION", false, Map.of("options", List.of("EXPRESSION", "INTENT_ROUTING"))));
                break;
            case TRANSFORM:
                fields.add(field("transformType", "转换类型", "select", "JSON_TO_XML", false, Map.of("options", List.of("JSON_TO_XML", "XML_TO_JSON", "TEMPLATE"))));
                fields.add(field("template", "转换模板", "textarea", "", false, "转换规则模板"));
                break;
            case OUTPUT_FORMAT:
                fields.add(field("outputFormat", "输出格式", "select", "TEXT", false, Map.of("options", List.of("TEXT", "JSON", "MARKDOWN", "HTML"))));
                fields.add(field("template", "格式化模板", "textarea", "", false, "输出格式化模板"));
                fields.add(field("prettyPrint", "美化输出", "boolean", true, false, "是否美化格式"));
                break;
            case MEMORY_SHORT_TERM:
                fields.add(field("maxMessages", "最大消息数", "number", 10, false, "短时记忆窗口大小"));
                fields.add(field("enableSlidingWindow", "滑动窗口", "boolean", true, false, "是否启用滑动窗口"));
                fields.add(field("messageExpiryTime", "消息过期时间(ms)", "number", 3600000L, false, "消息过期时间"));
                break;
            case AGENT_CALL:
                fields.add(field("targetAgentId", "目标Agent", "text", "", true, "调用的目标Agent标识"));
                fields.add(field("agentTimeout", "超时时间(ms)", "number", 30000L, false, "Agent调用超时"));
                fields.add(field("async", "异步调用", "boolean", false, false, "是否异步调用"));
                break;
            default:
                break;
        }
        return fields;
    }

    private NodeTypeMetaVO.FieldMeta field(String key, String label, String type, Object defaultValue, boolean required, Object extra) {
        return NodeTypeMetaVO.FieldMeta.builder()
                .key(key).label(label).type(type).defaultValue(defaultValue).required(required)
                .description("")
                .options(extra instanceof Map ? (Map<String, Object>) extra : null)
                .build();
    }
}
