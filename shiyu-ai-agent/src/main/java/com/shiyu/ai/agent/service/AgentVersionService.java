package com.shiyu.ai.agent.service;

import com.shiyu.ai.agent.request.VersionRequest;
import com.shiyu.ai.agent.vo.AgentVersionDetailVO;
import com.shiyu.ai.agent.vo.AgentVersionVO;

import java.util.List;

/**
 * Agent Version 接口
 */

public interface AgentVersionService {

    /**
     * Get Versions
     * @return 处理结果
     */
    List<AgentVersionVO> getVersions(String agentId);

    /**
     * Get Version Detail
     * @return 处理结果
     */
    AgentVersionDetailVO getVersionDetail(String agentId, Long versionId);

    /**
     * Create Version
     * @param VersionRequest VersionRequest
     * @return 处理结果
     */
    AgentVersionVO createVersion(String agentId, VersionRequest request);

    /**
     * Update Version
     * @param VersionRequest VersionRequest
     * @return 处理结果
     */
    AgentVersionVO updateVersion(String agentId, Long versionId, VersionRequest request);

    /**
     * Delete Version
     * @return 处理结果
     */
    void deleteVersion(String agentId, Long versionId);

    /**
     * Publish Version
     * @return 处理结果
     */
    void publishVersion(String agentId, Long versionId);

    /**
     * Archive Version
     * @return 处理结果
     */
    void archiveVersion(String agentId, Long versionId);

    /**
     * Activate Version
     * @return 处理结果
     */
    void activateVersion(String agentId, Long versionId);

    /**
     * Copy Version
     * @param VersionRequest VersionRequest
     * @return 处理结果
     */
    AgentVersionVO copyVersion(String agentId, VersionRequest request);
}
