package com.shiyu.ai.agent.biz.agent.service;

import com.shiyu.ai.agent.domain.request.VersionRequest;
import com.shiyu.ai.agent.domain.vo.AgentVersionDetailVO;
import com.shiyu.ai.agent.domain.vo.AgentVersionVO;

import java.util.List;

public interface AgentVersionService {

    List<AgentVersionVO> getVersions(String agentId);

    AgentVersionDetailVO getVersionDetail(String agentId, Long versionId);

    AgentVersionVO createVersion(String agentId, VersionRequest request);

    AgentVersionVO updateVersion(String agentId, Long versionId, VersionRequest request);

    void deleteVersion(String agentId, Long versionId);

    void publishVersion(String agentId, Long versionId);

    void archiveVersion(String agentId, Long versionId);

    void activateVersion(String agentId, Long versionId);

    AgentVersionVO copyVersion(String agentId, VersionRequest request);
}
