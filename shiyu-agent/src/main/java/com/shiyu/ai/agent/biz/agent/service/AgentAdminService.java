package com.shiyu.ai.agent.biz.agent.service;

import com.shiyu.ai.agent.domain.request.*;
import com.shiyu.ai.agent.domain.vo.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface AgentAdminService {

    Pair<Long, List<AgentVO>> getPage(Number pageNo, Number pageSize, String name, String status);

    AgentDetailVO getById(Long id);

    AgentVO create(AgentRequest request);

    AgentVO update(Long id, AgentRequest request);

    void deleteById(Long id);

    List<AgentVersionVO> getVersions(String agentId);

    AgentVersionDetailVO getVersionDetail(String agentId, Long versionId);

    AgentVersionVO createVersion(String agentId, VersionRequest request);

    AgentVersionVO updateVersion(String agentId, Long versionId, VersionRequest request);

    void deleteVersion(String agentId, Long versionId);

    void publishVersion(String agentId, Long versionId);

    void archiveVersion(String agentId, Long versionId);

    void activateVersion(String agentId, Long versionId);

    AgentVersionVO copyVersion(String agentId, VersionRequest request);

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

    List<NodeTypeMetaVO> getNodeTypes();

    /**
     * 获取所有启用 Agent 列表（下拉选项用）
     */
    List<IdNameOptionVO> listAllOptions();
}
