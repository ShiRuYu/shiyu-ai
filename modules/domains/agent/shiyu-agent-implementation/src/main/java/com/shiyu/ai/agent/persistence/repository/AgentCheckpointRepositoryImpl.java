package com.shiyu.ai.agent.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.domain.model.AgentCheckpointBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.agent.persistence.dataobject.AgentCheckpointDO;
import com.shiyu.ai.agent.persistence.mapper.AgentCheckpointMapper;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgentCheckpointRepositoryImpl implements com.shiyu.ai.agent.port.repository.AgentCheckpointRepository {

    @Resource
    private AgentCheckpointMapper agentCheckpointMapper;

    public void insert(TenantId tenantId, AgentCheckpointBO checkpoint) {
        requireTenant(tenantId);
        if (checkpoint == null) throw new IllegalArgumentException("checkpoint must not be null");
        checkpoint.setTenantId(tenantId.value());
        AgentCheckpointDO data = MapstructUtils.convert(checkpoint, AgentCheckpointDO.class);
        agentCheckpointMapper.insertSelective(data);
        checkpoint.setId(data.getId());
    }

    public AgentCheckpointBO selectByCheckpointId(TenantId tenantId, String checkpointId) {
        return MapstructUtils.convert(agentCheckpointMapper.selectOneByQuery(
            scope(tenantId).eq("checkpoint_id", checkpointId)), AgentCheckpointBO.class);
    }

    public AgentCheckpointBO selectLatestByExecutionId(TenantId tenantId, String executionId) {
        QueryWrapper qw = scope(tenantId);
        qw.eq("execution_id", executionId);
        qw.orderBy("create_time", false);
        qw.limit(1);
        return MapstructUtils.convert(agentCheckpointMapper.selectOneByQuery(qw), AgentCheckpointBO.class);
    }

    public void deleteByCheckpointId(TenantId tenantId, String checkpointId) {
        agentCheckpointMapper.deleteByQuery(
            scope(tenantId).eq("checkpoint_id", checkpointId));
    }

    public void deleteByExecutionId(TenantId tenantId, String executionId) {
        agentCheckpointMapper.deleteByQuery(
            scope(tenantId).eq("execution_id", executionId));
    }

    public List<AgentCheckpointBO> listByExecutionId(TenantId tenantId, String executionId) {
        QueryWrapper qw = scope(tenantId);
        qw.eq("execution_id", executionId);
        qw.orderBy("create_time", true);
        return MapstructUtils.convert(agentCheckpointMapper.selectListByQuery(qw), AgentCheckpointBO.class);
    }

    private QueryWrapper scope(TenantId tenantId) {
        requireTenant(tenantId);
        return new QueryWrapper().eq("tenant_id", tenantId.value());
    }

    private void requireTenant(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId must not be null");
    }
}
