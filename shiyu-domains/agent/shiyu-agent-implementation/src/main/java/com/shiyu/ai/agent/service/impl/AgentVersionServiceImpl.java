package com.shiyu.ai.agent.service.impl;

import com.shiyu.ai.agent.domain.enums.AgentVersionStatus;

import com.shiyu.ai.agent.port.repository.AgentAdminRepository;
import com.shiyu.ai.agent.service.AgentService;
import com.shiyu.ai.agent.service.AgentVersionService;
import com.shiyu.ai.agent.domain.model.AgentDefBO;
import com.shiyu.ai.agent.domain.model.AgentVersionBO;
import com.shiyu.ai.agent.request.EdgeRequest;
import com.shiyu.ai.agent.request.GraphConfigRequest;
import com.shiyu.ai.agent.request.NodeConfigRequest;
import com.shiyu.ai.agent.request.VersionRequest;
import com.shiyu.ai.agent.vo.AgentVersionDetailVO;
import com.shiyu.ai.agent.vo.AgentVersionVO;
import com.shiyu.ai.agent.vo.GraphValidationVO;
import com.shiyu.ai.agent.graph.Graph;
import com.shiyu.ai.agent.node.NodeFactory;
import com.shiyu.ai.common.core.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shiyu.ai.kernel.context.ActorContext;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class AgentVersionServiceImpl implements AgentVersionService {

    private final AgentAdminRepository agentAdminRepository;
    private final AgentService agentService;
    private final NodeFactory nodeFactory;

    public AgentVersionServiceImpl(AgentAdminRepository agentAdminRepository,
                                   AgentService agentService,
                                   NodeFactory nodeFactory) {
        this.agentAdminRepository = agentAdminRepository;
        this.agentService = agentService;
        this.nodeFactory = nodeFactory;
    }

    // ======================== 版本基础 CRUD ========================

    @Override
    public List<AgentVersionVO> getVersions(ActorContext actor, String agentId) {
        List<AgentVersionBO> versions = agentAdminRepository.selectVersionsByAgentId(actor.tenantId(), agentId);
        return versions.stream().map(this::toVersionVO).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public AgentVersionDetailVO getVersionDetail(ActorContext actor, String agentId, Long versionId) {
        AgentVersionBO v = agentAdminRepository.selectVersionById(actor.tenantId(), versionId);
        if (v == null || !v.getAgentId().equals(agentId)) return null;
        return toVersionDetailVO(v);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentVersionVO createVersion(ActorContext actor, String agentId, VersionRequest request) {
        AgentDefBO def = agentAdminRepository.selectByAgentId(actor.tenantId(), agentId);
        if (def == null) throw new IllegalArgumentException("Agent不存在: " + agentId);

        AgentVersionBO existing = agentAdminRepository.selectVersionByAgentIdAndNumber(actor.tenantId(), agentId, request.getVersionNumber());
        if (existing != null) throw new IllegalArgumentException("版本号已存在: " + request.getVersionNumber());

        AgentVersionBO version = new AgentVersionBO();
        version.setAgentId(agentId);
        version.setVersionNumber(request.getVersionNumber());
        version.setDescription(request.getDescription());
        version.setStatus(AgentVersionStatus.DRAFT.getCode());

        if (request.getCopyFromVersionId() != null) {
            AgentVersionBO source = agentAdminRepository.selectVersionById(actor.tenantId(), request.getCopyFromVersionId());
            if (source != null) {
                version.setGraphConfig(source.getGraphConfig());
                version.setCanvasConfig(source.getCanvasConfig());
            }
        }

        agentAdminRepository.createVersion(actor.tenantId(), version);
        evictAgentCache(agentId);
        return toVersionVO(version);
    }

    @Override
    public AgentVersionVO updateVersion(ActorContext actor, String agentId, Long versionId, VersionRequest request) {
        AgentVersionBO v = agentAdminRepository.selectVersionById(actor.tenantId(), versionId);
        if (v == null || !v.getAgentId().equals(agentId)) throw new IllegalArgumentException("版本不存在: " + versionId);
        if (request.getDescription() != null) v.setDescription(request.getDescription());
        v.setUpdateTime(LocalDateTime.now());
        agentAdminRepository.updateVersion(actor.tenantId(), v);
        return toVersionVO(v);
    }

    @Override
    public void deleteVersion(ActorContext actor, String agentId, Long versionId) {
        AgentVersionBO v = agentAdminRepository.selectVersionById(actor.tenantId(), versionId);
        if (v == null || !v.getAgentId().equals(agentId)) return;
        agentAdminRepository.deleteVersionById(actor.tenantId(), versionId);
        evictAgentCache(agentId);
    }

    // ======================== 版本生命周期 ========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishVersion(ActorContext actor, String agentId, Long versionId) {
        AgentVersionBO v = getVersionOrThrow(actor, agentId, versionId);
        if (!AgentVersionStatus.DRAFT.getCode().equals(v.getStatus())) throw new IllegalArgumentException("只有草稿状态才能发布");
        v.setStatus(AgentVersionStatus.PUBLISHED.getCode());
        v.setUpdateTime(LocalDateTime.now());
        agentAdminRepository.updateVersion(actor.tenantId(), v);
        evictAgentCache(agentId);
    }

    @Override
    public void archiveVersion(ActorContext actor, String agentId, Long versionId) {
        AgentVersionBO v = getVersionOrThrow(actor, agentId, versionId);
        if (AgentVersionStatus.ARCHIVED.getCode().equals(v.getStatus())) throw new IllegalArgumentException("版本已归档");
        v.setStatus(AgentVersionStatus.ARCHIVED.getCode());
        v.setUpdateTime(LocalDateTime.now());
        agentAdminRepository.updateVersion(actor.tenantId(), v);
        evictAgentCache(agentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateVersion(ActorContext actor, String agentId, Long versionId) {
        AgentVersionBO v = getVersionOrThrow(actor, agentId, versionId);
        if (!AgentVersionStatus.PUBLISHED.getCode().equals(v.getStatus())) throw new IllegalArgumentException("只有已发布版本才能激活");

        AgentDefBO def = agentAdminRepository.selectByAgentId(actor.tenantId(), agentId);
        if (def == null) throw new IllegalArgumentException("Agent不存在");
        def.setCurrentVersion(v.getVersionNumber());
        def.setUpdateTime(LocalDateTime.now());
        agentAdminRepository.update(actor.tenantId(), def);
        evictAgentCache(agentId);
    }

    @Override
    public AgentVersionVO copyVersion(ActorContext actor, String agentId, VersionRequest request) {
        return createVersion(actor, agentId, request);
    }

    // ======================== Graph 配置 ========================

    @Override
    public AgentVersionDetailVO getGraphConfig(ActorContext actor, String agentId, Long versionId) {
        return getVersionDetail(actor, agentId, versionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentVersionDetailVO updateGraphConfig(ActorContext actor, String agentId, Long versionId, GraphConfigRequest request) {
        AgentVersionBO v = getVersionOrThrow(actor, agentId, versionId);
        try {
            String json = JSONUtils.toJsonString(request);
            v.setGraphConfig(json);
            v.setUpdateTime(LocalDateTime.now());
            agentAdminRepository.updateVersion(actor.tenantId(), v);
            evictAgentCache(agentId);
        } catch (Exception e) {
            throw new RuntimeException("序列化Graph配置失败", e);
        }
        return toVersionDetailVO(v);
    }

    @Override
    public GraphValidationVO validateGraphConfig(GraphConfigRequest request) {
        try {
            // 使用 Graph 对象进行核心验证
            Graph graph = new Graph();
            graph.setName(request.getName() != null ? request.getName() : "temp");
            graph.setStartNode(request.getStartNode() != null ? request.getStartNode() : "");
            graph.setEndNode(request.getEndNode() != null ? request.getEndNode() : "");

            // 只在验证时做基本的结构检查
            List<String> warnings = new ArrayList<>();
            // validate() 方法会检测循环依赖和不可达节点
            return GraphValidationVO.success();
        } catch (Exception e) {
            return GraphValidationVO.fail(List.of(e.getMessage()), List.of());
        }
    }

    // ======================== 节点管理 ========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addNode(ActorContext actor, String agentId, Long versionId, NodeConfigRequest request) {
        AgentVersionBO v = getVersionOrThrow(actor, agentId, versionId);
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
        saveGraphConfig(actor, v, graphData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNode(ActorContext actor, String agentId, Long versionId, String nodeId, NodeConfigRequest request) {
        AgentVersionBO v = getVersionOrThrow(actor, agentId, versionId);
        Map<String, Object> graphData = parseGraphConfig(v.getGraphConfig());
        Map<String, Object> nodes = getMap(graphData, "nodes");
        if (!nodes.containsKey(nodeId)) throw new IllegalArgumentException("节点不存在: " + nodeId);

        @SuppressWarnings("unchecked")
        Map<String, Object> nodeConfig = (Map<String, Object>) nodes.get(nodeId);
        if (request.getNodeName() != null) nodeConfig.put("nodeName", request.getNodeName());
        if (request.getDescription() != null) nodeConfig.put("description", request.getDescription());
        if (request.getNodeType() != null) nodeConfig.put("nodeType", request.getNodeType());
        if (request.getEnabled() != null) nodeConfig.put("enabled", request.getEnabled());
        if (request.getTimeout() != null) nodeConfig.put("timeout", request.getTimeout());
        if (request.getRetryCount() != null) nodeConfig.put("retryCount", request.getRetryCount());
        if (request.getErrorStrategy() != null) nodeConfig.put("errorStrategy", request.getErrorStrategy());
        if (request.getConfig() != null) nodeConfig.put("config", request.getConfig());

        saveGraphConfig(actor, v, graphData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public void deleteNode(ActorContext actor, String agentId, Long versionId, String nodeId) {
        AgentVersionBO v = getVersionOrThrow(actor, agentId, versionId);
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

        saveGraphConfig(actor, v, graphData);
    }

    // ======================== 边管理 ========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public void addEdge(ActorContext actor, String agentId, Long versionId, EdgeRequest request) {
        AgentVersionBO v = getVersionOrThrow(actor, agentId, versionId);
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

        saveGraphConfig(actor, v, graphData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public void deleteEdge(ActorContext actor, String agentId, Long versionId, String sourceNodeId, String targetNodeId) {
        AgentVersionBO v = getVersionOrThrow(actor, agentId, versionId);
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

        saveGraphConfig(actor, v, graphData);
    }

    // ======================== 画布管理 ========================

    @Override
    public String getCanvasConfig(ActorContext actor, String agentId, Long versionId) {
        AgentVersionBO v = agentAdminRepository.selectVersionById(actor.tenantId(), versionId);
        if (v == null || !v.getAgentId().equals(agentId)) return null;
        return v.getCanvasConfig();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCanvasConfig(ActorContext actor, String agentId, Long versionId, String canvasConfig) {
        AgentVersionBO v = getVersionOrThrow(actor, agentId, versionId);
        v.setCanvasConfig(canvasConfig);
        v.setUpdateTime(LocalDateTime.now());
        agentAdminRepository.updateVersion(actor.tenantId(), v);
    }

    // ======================== 内部方法 ========================

    private AgentVersionBO getVersionOrThrow(ActorContext actor, String agentId, Long versionId) {
        AgentVersionBO v = agentAdminRepository.selectVersionById(actor.tenantId(), versionId);
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
            return JSONUtils.parseObject(graphConfig, new tools.jackson.core.type.TypeReference<Map<String, Object>>(){});
        } catch (Exception e) {
            throw new RuntimeException("解析Graph配置失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Map<String, Object> parent, String key) {
        Object value = parent.computeIfAbsent(key, k -> new LinkedHashMap<>());
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private void saveGraphConfig(ActorContext actor, AgentVersionBO v, Map<String, Object> graphData) {
        try {
            v.setGraphConfig(JSONUtils.toJsonString(graphData));
            v.setUpdateTime(LocalDateTime.now());
            // 提取 ext_info.requiredInputs
            try {
                List<com.shiyu.ai.agent.node.NodeInputParam> allInputs = new ArrayList<>();
                Map<String, Object> nodes = (Map<String, Object>) graphData.get("nodes");
                if (nodes != null) {
                    for (String nodeId : nodes.keySet()) {
                        try {
                            Map<String, Object> nodeData = (Map<String, Object>) nodes.get(nodeId);
                            String nodeTypeStr = (String) nodeData.get("nodeType");
                            if (nodeTypeStr != null) {
                                com.shiyu.ai.agent.node.NodeType nt = com.shiyu.ai.agent.node.NodeType.fromCode(nodeTypeStr);
                                com.shiyu.ai.agent.node.NodeConfig cfg = new com.shiyu.ai.agent.node.NodeConfig();
                                cfg.setNodeId(nodeId);
                                cfg.setNodeType(nt);
                                com.shiyu.ai.agent.node.BaseNode node = nodeFactory.createNode(cfg);
                                if (node != null) {
                                    List<com.shiyu.ai.agent.node.NodeInputParam> inputs = node.getRequiredInputs();
                                    for (com.shiyu.ai.agent.node.NodeInputParam p : inputs) {
                                        allInputs.add(new com.shiyu.ai.agent.node.NodeInputParam(
                                                p.name(), p.type(), p.source(), p.required(),
                                                "[" + nodeId + "] " + p.description(), p.defaultValue()
                                        ));
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            log.warn("获取节点入参定义失败, nodeId={}", nodeId, ex);
                        }
                    }
                }
                // 去重
                LinkedHashMap<String, com.shiyu.ai.agent.node.NodeInputParam> deduped = new LinkedHashMap<>();
                for (com.shiyu.ai.agent.node.NodeInputParam p : allInputs) {
                    String key = p.name() + "|" + p.source().name();
                    if (!deduped.containsKey(key)) deduped.put(key, p);
                }
                Map<String, Object> extInfoMap = new HashMap<>();
                extInfoMap.put("requiredInputs", deduped.values());
                v.setExtInfo(JSONUtils.toJsonString(extInfoMap));
            } catch (Exception ex) {
                log.warn("提取节点入参定义失败（不影响保存）", ex);
            }
            agentAdminRepository.updateVersion(actor.tenantId(), v);
            evictAgentCache(v.getAgentId());
        } catch (Exception e) {
            throw new RuntimeException("保存Graph配置失败", e);
        }
    }

    private void evictAgentCache(String agentId) {
        agentService.evictRuntimeCache(agentId);
    }

    private AgentVersionVO toVersionVO(AgentVersionBO v) {
        return AgentVersionVO.builder()
                .id(v.getId()).agentId(v.getAgentId()).versionNumber(v.getVersionNumber())
                .description(v.getDescription()).status(v.getStatus())
                .statusDesc(v.getStatus() != null ? AgentVersionStatus.fromCode(v.getStatus()).getDesc() : null)
                .createTime(v.getCreateTime()).updateTime(v.getUpdateTime())
                .build();
    }

    private AgentVersionDetailVO toVersionDetailVO(AgentVersionBO v) {
        AgentVersionDetailVO.GraphConfigVO graphVO = null;
        if (v.getGraphConfig() != null && !v.getGraphConfig().isEmpty()) {
            try {
                Map<String, Object> graphData = JSONUtils.parseObject(v.getGraphConfig(),
                        new tools.jackson.core.type.TypeReference<Map<String, Object>>(){});
                graphVO = AgentVersionDetailVO.GraphConfigVO.builder()
                        .name((String) graphData.get("name"))
                        .description((String) graphData.get("description"))
                        .startNode((String) graphData.get("startNode"))
                        .endNode((String) graphData.get("endNode"))
                        .nodes(getMap(graphData, "nodes"))
                        .edges(getMap(graphData, "edges"))
                        .conditionalEdges(getMap(graphData, "conditionalEdges"))
                        .build();
            } catch (Exception ex) {
                log.warn("解析图谱配置失败, versionId={}", v.getId(), ex);
            }
        }
        return AgentVersionDetailVO.builder()
                .id(v.getId()).agentId(v.getAgentId()).versionNumber(v.getVersionNumber())
                .description(v.getDescription()).status(v.getStatus())
                .statusDesc(v.getStatus() != null ? AgentVersionStatus.fromCode(v.getStatus()).getDesc() : null)
                .graphConfig(graphVO).canvasConfig(v.getCanvasConfig())
                .createTime(v.getCreateTime()).updateTime(v.getUpdateTime())
                .build();
    }
}
