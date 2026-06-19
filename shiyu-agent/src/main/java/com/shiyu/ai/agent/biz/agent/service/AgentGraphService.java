package com.shiyu.ai.agent.biz.agent.service;

import com.shiyu.ai.agent.domain.request.EdgeRequest;
import com.shiyu.ai.agent.domain.request.GraphConfigRequest;
import com.shiyu.ai.agent.domain.request.NodeConfigRequest;
import com.shiyu.ai.agent.domain.vo.AgentVersionDetailVO;
import com.shiyu.ai.agent.domain.vo.GraphValidationVO;

public interface AgentGraphService {

    AgentVersionDetailVO getGraphConfig(String agentId, Long versionId);

    AgentVersionDetailVO updateGraphConfig(String agentId, Long versionId, GraphConfigRequest request);

    GraphValidationVO validateGraphConfig(GraphConfigRequest request);

    void addNode(String agentId, Long versionId, NodeConfigRequest request);

    void updateNode(String agentId, Long versionId, String nodeId, NodeConfigRequest request);

    void deleteNode(String agentId, Long versionId, String nodeId);

    void addEdge(String agentId, Long versionId, EdgeRequest request);

    void deleteEdge(String agentId, Long versionId, String sourceNodeId, String targetNodeId);

    String getCanvasConfig(String agentId, Long versionId);

    void updateCanvasConfig(String agentId, Long versionId, String canvasConfig);
}
