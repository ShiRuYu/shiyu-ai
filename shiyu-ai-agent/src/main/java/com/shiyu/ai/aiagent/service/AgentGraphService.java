package com.shiyu.ai.aiagent.service;

import com.shiyu.ai.aiagent.request.EdgeRequest;
import com.shiyu.ai.aiagent.request.GraphConfigRequest;
import com.shiyu.ai.aiagent.request.NodeConfigRequest;
import com.shiyu.ai.aiagent.vo.AgentVersionDetailVO;
import com.shiyu.ai.aiagent.vo.GraphValidationVO;

/**
 * Agent Graph 接口
 */

public interface AgentGraphService {

    /**
     * Get Graph Config
     * @return 处理结果
     */
    AgentVersionDetailVO getGraphConfig(String agentId, Long versionId);

    /**
     * Update Graph Config
     * @param GraphConfigRequest GraphConfigRequest
     * @return 处理结果
     */
    AgentVersionDetailVO updateGraphConfig(String agentId, Long versionId, GraphConfigRequest request);

    /**
     * Validate Graph Config
     * @param GraphConfigRequest GraphConfigRequest
     * @return 处理结果
     */
    GraphValidationVO validateGraphConfig(GraphConfigRequest request);

    /**
     * Add Node
     * @param NodeConfigRequest NodeConfigRequest
     * @return 处理结果
     */
    void addNode(String agentId, Long versionId, NodeConfigRequest request);

    /**
     * Update Node
     * @param NodeConfigRequest NodeConfigRequest
     * @return 处理结果
     */
    void updateNode(String agentId, Long versionId, String nodeId, NodeConfigRequest request);

    /**
     * Delete Node
     * @return 处理结果
     */
    void deleteNode(String agentId, Long versionId, String nodeId);

    /**
     * Add Edge
     * @param EdgeRequest EdgeRequest
     * @return 处理结果
     */
    void addEdge(String agentId, Long versionId, EdgeRequest request);

    /**
     * Delete Edge
     * @return 处理结果
     */
    void deleteEdge(String agentId, Long versionId, String sourceNodeId, String targetNodeId);

    /**
     * Get Canvas Config
     * @return 处理结果
     */
    String getCanvasConfig(String agentId, Long versionId);

    /**
     * Update Canvas Config
     * @return 处理结果
     */
    void updateCanvasConfig(String agentId, Long versionId, String canvasConfig);
}
