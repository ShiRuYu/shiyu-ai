package com.shiyu.ai.agent.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.domain.model.ExecutionTimelineBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.agent.persistence.dataobject.ExecutionTimelineDO;
import com.shiyu.ai.agent.persistence.mapper.ExecutionTimelineMapper;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExecutionTimelineRepositoryImpl implements com.shiyu.ai.agent.port.repository.ExecutionTimelineRepository {

    @Resource
    private ExecutionTimelineMapper executionTimelineMapper;

    public void insert(TenantId tenantId, ExecutionTimelineBO timeline) {
        requireTenant(tenantId);
        timeline.setTenantId(tenantId.value());
        executionTimelineMapper.insertSelective(MapstructUtils.convert(timeline, ExecutionTimelineDO.class));
    }

    public List<ExecutionTimelineBO> listByExecutionId(TenantId tenantId, String executionId) {
        requireTenant(tenantId);
        QueryWrapper qw = new QueryWrapper();
        qw.eq("tenant_id", tenantId.value());
        qw.eq("execution_id", executionId);
        qw.orderBy("id", true);
        return MapstructUtils.convert(executionTimelineMapper.selectListByQuery(qw), ExecutionTimelineBO.class);
    }

    private static void requireTenant(TenantId tenantId) {
        if (tenantId == null || tenantId.value() <= 0) {
            throw new IllegalArgumentException("tenantId is required");
        }
    }
}
