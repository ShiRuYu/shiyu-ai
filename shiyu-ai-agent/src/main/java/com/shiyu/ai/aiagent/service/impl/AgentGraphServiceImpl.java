package com.shiyu.ai.aiagent.service.impl;

import com.shiyu.ai.aiagent.repository.AgentAdminRepository;
import com.shiyu.ai.aiagent.service.AgentGraphService;
import com.shiyu.ai.aiagent.service.AgentService;
import com.shiyu.ai.aiagent.bo.AgentVersionBO;
import com.shiyu.ai.aiagent.request.EdgeRequest;
import com.shiyu.ai.aiagent.request.GraphConfigRequest;
import com.shiyu.ai.aiagent.request.NodeConfigRequest;
import com.shiyu.ai.aiagent.vo.AgentVersionDetailVO;
import com.shiyu.ai.aiagent.vo.GraphValidationVO;
import com.shiyu.ai.aiagent.graph.Graph;
import com.shiyu.ai.common.core.utils.JSONUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class AgentGraphServiceImpl implements AgentGraphService {

    @Resource
    private AgentAdminRepository agentAdminRepository;

    @Resource
    private AgentService agentService;

    @Override
    public AgentVersionDetailVO getGraphConfig(String agentId, Long versionId) {
        AgentVersionBO v = agentAdminRepository.selectVersionById(versionId);
        if (v == null || !v.getAgentId().equals(agentId)) return null;
        return toVersionDetailVO(v);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentVersionDetailVO updateGraphConfig(String agentId, Long versionId, GraphConfigRequest request) {
        AgentVersionBO v = getVersionOrThrow(agentId, versionId);
        try {
            String json = JSONUtils.toJsonString(request);
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
        AgentVersionBO v = getVersionOrThrow(agentId, versionId);
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
        AgentVersionBO v = getVersionOrThrow(agentId, versionId);
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
        AgentVersionBO v = getVersionOrThrow(agentId, versionId);
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
        AgentVersionBO v = getVersionOrThrow(agentId, versionId);
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
        AgentVersionBO v = getVersionOrThrow(agentId, versionId);
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
        AgentVersionBO v = agentAdminRepository.selectVersionById(versionId);
        if (v == null || !v.getAgentId().equals(agentId)) return null;
        return v.getCanvasConfig();
    }

    @Override
    public void updateCanvasConfig(String agentId, Long versionId, String canvasConfig) {
        AgentVersionBO v = getVersionOrThrow(agentId, versionId);
        v.setCanvasConfig(canvasConfig);
        v.setUpdateTime(LocalDateTime.now());
        agentAdminRepository.updateVersion(v);
    }

    // ========== Private helpers ==========

    private AgentVersionBO getVersionOrThrow(String agentId, Long versionId) {
        AgentVersionBO v = agentAdminRepository.selectVersionById(versionId);
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

    private void saveGraphConfig(AgentVersionBO v, Map<String, Object> graphData) {
        try {
            v.setGraphConfig(JSONUtils.toJsonString(graphData));
            v.setUpdateTime(LocalDateTime.now());
            agentAdminRepository.updateVersion(v);
            evictAgentCache(v.getAgentId());
        } catch (Exception e) {
            throw new RuntimeException("保存Graph配置失败", e);
        }
    }

    private void evictAgentCache(String agentId) {
        agentService.evictRuntimeCache(agentId);
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
}
