package com.shiyu.ai.agent.port.repository;

import com.shiyu.ai.agent.domain.model.AgentDefBO;
import com.shiyu.ai.agent.domain.model.AgentVersionBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface AgentAdminRepository {
    Pair<Long, List<AgentDefBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize, String name, Integer status);
    AgentDefBO selectById(TenantId tenantId, Long id);
    AgentDefBO selectByAgentId(TenantId tenantId, String agentId);
    List<AgentDefBO> selectAllActive(TenantId tenantId);
    AgentDefBO create(TenantId tenantId, AgentDefBO agentDefBO);
    AgentDefBO update(TenantId tenantId, AgentDefBO agentDefBO);
    void deleteById(TenantId tenantId, Long id);
    void deleteByAgentId(TenantId tenantId, String agentId);
    List<AgentVersionBO> selectVersionsByAgentId(TenantId tenantId, String agentId);
    AgentVersionBO selectVersionById(TenantId tenantId, Long versionId);
    AgentVersionBO selectVersionByAgentIdAndNumber(TenantId tenantId, String agentId, String versionNumber);
    AgentVersionBO createVersion(TenantId tenantId, AgentVersionBO versionBO);
    AgentVersionBO updateVersion(TenantId tenantId, AgentVersionBO versionBO);
    void deleteVersionById(TenantId tenantId, Long versionId);
}
