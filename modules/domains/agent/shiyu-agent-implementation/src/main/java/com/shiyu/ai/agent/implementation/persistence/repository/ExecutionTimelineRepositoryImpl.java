package com.shiyu.ai.agent.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.implementation.domain.model.ExecutionTimelineBO;
import com.shiyu.ai.agent.implementation.persistence.dataobject.ExecutionTimelineDO;
import com.shiyu.ai.agent.implementation.persistence.mapper.ExecutionTimelineMapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@code ExecutionTimelineRepositoryImpl} 实现智能体模块的持久化端口，负责在领域对象与存储模型之间转换。
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
        if (tenantId == null || tenantId.value() <= 0) {
            throw new IllegalArgumentException("tenantId is required");
        }
    }
}
