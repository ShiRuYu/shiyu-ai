package com.shiyu.ai.agent.service;

import com.shiyu.ai.agent.request.EdgeRequest;
import com.shiyu.ai.agent.request.GraphConfigRequest;
import com.shiyu.ai.agent.request.NodeConfigRequest;
import com.shiyu.ai.agent.request.VersionRequest;
import com.shiyu.ai.agent.vo.AgentVersionDetailVO;
import com.shiyu.ai.agent.vo.AgentVersionVO;
import com.shiyu.ai.agent.vo.GraphValidationVO;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * Agent Version 接口
 * 合并了版本管理和 Graph 配置管理（节点、边、画布）。
 */
public interface AgentVersionService {

    // ========== 版本基础 CRUD ==========

    List<AgentVersionVO> getVersions(ActorContext actor, String agentId);

    AgentVersionDetailVO getVersionDetail(ActorContext actor, String agentId, Long versionId);

    AgentVersionVO createVersion(ActorContext actor, String agentId, VersionRequest request);

    AgentVersionVO updateVersion(ActorContext actor, String agentId, Long versionId, VersionRequest request);

    void deleteVersion(ActorContext actor, String agentId, Long versionId);

    // ========== 版本生命周期 ==========

    void publishVersion(ActorContext actor, String agentId, Long versionId);

    void archiveVersion(ActorContext actor, String agentId, Long versionId);

    void activateVersion(ActorContext actor, String agentId, Long versionId);

    AgentVersionVO copyVersion(ActorContext actor, String agentId, VersionRequest request);

    // ========== Graph 配置 ==========

    AgentVersionDetailVO getGraphConfig(ActorContext actor, String agentId, Long versionId);

    AgentVersionDetailVO updateGraphConfig(ActorContext actor, String agentId, Long versionId, GraphConfigRequest request);

    GraphValidationVO validateGraphConfig(GraphConfigRequest request);

    // ========== 节点管理 ==========

    void addNode(ActorContext actor, String agentId, Long versionId, NodeConfigRequest request);

    void updateNode(ActorContext actor, String agentId, Long versionId, String nodeId, NodeConfigRequest request);

    void deleteNode(ActorContext actor, String agentId, Long versionId, String nodeId);

    // ========== 边管理 ==========

    void addEdge(ActorContext actor, String agentId, Long versionId, EdgeRequest request);

    void deleteEdge(ActorContext actor, String agentId, Long versionId, String sourceNodeId, String targetNodeId);

    // ========== 画布管理 ==========

    String getCanvasConfig(ActorContext actor, String agentId, Long versionId);

    void updateCanvasConfig(ActorContext actor, String agentId, Long versionId, String canvasConfig);
}
