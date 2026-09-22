package com.shiyu.ai.agent.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.implementation.domain.model.ExecutionTimelineBO;
import com.shiyu.ai.agent.implementation.persistence.dataobject.ExecutionTimelineDO;
import com.shiyu.ai.agent.implementation.persistence.mapper.ExecutionTimelineMapper;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 负责 Execution 时间线 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Component
public class ExecutionTimelineRepositoryImpl
        implements com.shiyu.ai.agent.implementation.port.repository.ExecutionTimelineRepository {

    /**
     * executionTimelineMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Resource private ExecutionTimelineMapper executionTimelineMapper;

    public void insert(TenantId tenantId, ExecutionTimelineBO timeline) {
        requireTenant(tenantId);
        timeline.setTenantId(tenantId.value());
        executionTimelineMapper.insertSelective(
                MapstructUtils.convert(timeline, ExecutionTimelineDO.class));
    }

    public List<ExecutionTimelineBO> listByExecutionId(TenantId tenantId, String executionId) {
        requireTenant(tenantId);
        QueryWrapper qw = new QueryWrapper();
        qw.eq("tenant_id", tenantId.value());
        qw.eq("execution_id", executionId);
        qw.orderBy("id", true);
        return MapstructUtils.convert(
                executionTimelineMapper.selectListByQuery(qw), ExecutionTimelineBO.class);
    }

    private static void requireTenant(TenantId tenantId) {
        TenantScope.requireMatches(tenantId);
    }
}
