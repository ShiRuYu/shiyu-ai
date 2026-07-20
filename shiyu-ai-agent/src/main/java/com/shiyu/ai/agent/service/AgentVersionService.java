package com.shiyu.ai.agent.service;

import com.shiyu.ai.agent.request.EdgeRequest;
import com.shiyu.ai.agent.request.GraphConfigRequest;
import com.shiyu.ai.agent.request.NodeConfigRequest;
import com.shiyu.ai.agent.request.VersionRequest;
import com.shiyu.ai.agent.vo.AgentVersionDetailVO;
import com.shiyu.ai.agent.vo.AgentVersionVO;
import com.shiyu.ai.agent.vo.GraphValidationVO;

import java.util.List;

/**
 * Agent Version 接口
 * 合并了版本管理和 Graph 配置管理（节点、边、画布）。
 */
public interface AgentVersionService {

    // ========== 版本基础 CRUD ==========

    List<AgentVersionVO> getVersions(String agentId);

    AgentVersionDetailVO getVersionDetail(String agentId, Long versionId);

    AgentVersionVO createVersion(String agentId, VersionRequest request);

    AgentVersionVO updateVersion(String agentId, Long versionId, VersionRequest request);

    void deleteVersion(String agentId, Long versionId);

    // ========== 版本生命周期 ==========

    void publishVersion(String agentId, Long versionId);

    void archiveVersion(String agentId, Long versionId);

    void activateVersion(String agentId, Long versionId);

    AgentVersionVO copyVersion(String agentId, VersionRequest request);

    // ========== Graph 配置 ==========

    AgentVersionDetailVO getGraphConfig(String agentId, Long versionId);

    AgentVersionDetailVO updateGraphConfig(String agentId, Long versionId, GraphConfigRequest request);

    GraphValidationVO validateGraphConfig(GraphConfigRequest request);

    // ========== 节点管理 ==========

    void addNode(String agentId, Long versionId, NodeConfigRequest request);

    void updateNode(String agentId, Long versionId, String nodeId, NodeConfigRequest request);

    void deleteNode(String agentId, Long versionId, String nodeId);

    // ========== 边管理 ==========

    void addEdge(String agentId, Long versionId, EdgeRequest request);

    void deleteEdge(String agentId, Long versionId, String sourceNodeId, String targetNodeId);

    // ========== 画布管理 ==========

    String getCanvasConfig(String agentId, Long versionId);

    void updateCanvasConfig(String agentId, Long versionId, String canvasConfig);
}
