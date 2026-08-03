package com.shiyu.ai.agent.port.repository;

import com.shiyu.ai.agent.domain.model.AgentDefBO;
import com.shiyu.ai.agent.domain.model.AgentVersionBO;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface AgentAdminRepository {
    Pair<Long, List<AgentDefBO>> selectPage(Number pageNo, Number pageSize, String name, Integer status);
    AgentDefBO selectById(Long id);
    AgentDefBO selectByAgentId(String agentId);
    List<AgentDefBO> selectAllActive();
    AgentDefBO create(AgentDefBO agentDefBO);
    AgentDefBO update(AgentDefBO agentDefBO);
    void deleteById(Long id);
    void deleteByAgentId(String agentId);
    List<AgentVersionBO> selectVersionsByAgentId(String agentId);
    AgentVersionBO selectVersionById(Long versionId);
    AgentVersionBO selectVersionByAgentIdAndNumber(String agentId, String versionNumber);
    AgentVersionBO createVersion(AgentVersionBO versionBO);
    AgentVersionBO updateVersion(AgentVersionBO versionBO);
    void deleteVersionById(Long versionId);
}
