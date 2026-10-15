package com.shiyu.ai.agent.implementation.graph;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.implementation.request.GraphConfigRequest;
import com.shiyu.ai.agent.implementation.vo.GraphValidationVO;
import com.shiyu.ai.common.foundation.utils.JSONUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 校验发布和版本激活共用的图结构。 */
public final class AgentGraphConfigValidator {
    private AgentGraphConfigValidator() {}

    /** 判断持久化的图 JSON 是否格式正确且结构有效。 */
    public static boolean isValidJson(String graphConfig) {
        if (graphConfig == null || graphConfig.isBlank()) return false;
        try {
            GraphConfigRequest request = JSONUtils.parseObject(
                    graphConfig,
                    new tools.jackson.core.type.TypeReference<GraphConfigRequest>() {});
            return validate(request).isValid();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    /** 返回草稿图的结构错误和不可达节点警告。 */
    public static GraphValidationVO validate(GraphConfigRequest request) {
        try {
            if (request == null) {
                throw new IllegalArgumentException("graph config is required");
            }

            Graph graph = new Graph();
            graph.setName(request.getName() == null ? "temp" : request.getName());
            graph.setStartNode(request.getStartNode());
            graph.setEndNode(request.getEndNode());

            Map<String, BaseNode> nodes = new LinkedHashMap<>();
            if (request.getNodes() != null) {
                request.getNodes().forEach((nodeId, configuration) -> {
                    if (configuration == null) {
                        throw new IllegalArgumentException("节点配置不能为空: " + nodeId);
                    }
                    nodes.put(nodeId, null);
                });
            }
            graph.setNodes(nodes);
            graph.setEdges(
                    request.getEdges() == null
                            ? new LinkedHashMap<>()
                            : new LinkedHashMap<>(request.getEdges()));

            Map<String, ConditionEdge> conditionalEdges = new LinkedHashMap<>();
            if (request.getConditionalEdges() != null) {
                request.getConditionalEdges()
                        .forEach(
                                (sourceId, config) -> {
                                    if (config == null
                                            || ((config.getNodeMappings() == null
                                                            || config.getNodeMappings().isEmpty())
                                                    && (config.getDefaultTarget() == null
                                                            || config.getDefaultTarget().isBlank()))) {
                                        throw new IllegalArgumentException(
                                                "条件边必须配置映射或默认目标: " + sourceId);
                                    }
                                    ConditionEdge edge =
                                            ConditionEdge.builder()
                                                    .from(sourceId)
                                                    .defaultTarget(config.getDefaultTarget())
                                                    .nodeMappings(
                                                            config.getNodeMappings() == null
                                                                    ? new LinkedHashMap<>()
                                                                    : new LinkedHashMap<>(
                                                                            config.getNodeMappings()))
                                                    .functionCondition(state -> null)
                                                    .build();
                                    conditionalEdges.put(sourceId, edge);
                                });
            }
            graph.setConditionalEdges(conditionalEdges);
            graph.validate();
            List<String> warnings =
                    graph.findUnreachableNodes().stream()
                            .sorted()
                            .map(nodeId -> "节点不可达: " + nodeId)
                            .toList();
            return GraphValidationVO.success(warnings);
        } catch (Exception exception) {
            return GraphValidationVO.fail(List.of("图配置校验失败，请检查配置"), List.of());
        }
    }
}
