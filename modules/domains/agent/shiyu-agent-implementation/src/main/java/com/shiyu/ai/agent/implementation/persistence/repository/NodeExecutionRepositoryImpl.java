package com.shiyu.ai.agent.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.implementation.domain.model.NodeExecutionBO;
import com.shiyu.ai.agent.implementation.persistence.dataobject.NodeExecutionDO;
import com.shiyu.ai.agent.implementation.persistence.mapper.NodeExecutionMapper;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 负责 Node Execution 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Component
public class NodeExecutionRepositoryImpl
        implements com.shiyu.ai.agent.implementation.port.repository.NodeExecutionRepository {

    /**
     * nodeExecutionMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Resource private NodeExecutionMapper nodeExecutionMapper;

    public void insert(TenantId tenantId, NodeExecutionBO bo) {
        requireTenant(tenantId);
        bo.setTenantId(tenantId.value());
        NodeExecutionDO d = MapstructUtils.convert(bo, NodeExecutionDO.class);
        d.setTenantId(tenantId.value());
        nodeExecutionMapper.insertSelective(d);
        bo.setId(d.getId());
    }

    public void update(TenantId tenantId, NodeExecutionBO bo) {
        requireTenant(tenantId);
        NodeExecutionDO d = MapstructUtils.convert(bo, NodeExecutionDO.class);
        d.setTenantId(tenantId.value());
        QueryWrapper query =
                QueryWrapper.create()
                        .eq(NodeExecutionDO::getTenantId, tenantId.value())
                        .eq(NodeExecutionDO::getId, bo.getId());
        nodeExecutionMapper.updateByQuery(d, query);
    }

    public List<NodeExecutionBO> selectByExecutionId(TenantId tenantId, String executionId) {
        requireTenant(tenantId);
        QueryWrapper qw = new QueryWrapper();
        qw.eq(NodeExecutionDO::getTenantId, tenantId.value());
        qw.eq(NodeExecutionDO::getExecutionId, executionId);
        qw.orderBy(NodeExecutionDO::getStartTime, true);
        List<NodeExecutionDO> doList = nodeExecutionMapper.selectListByQuery(qw);
        return MapstructUtils.convert(doList, NodeExecutionBO.class);
    }

    private static void requireTenant(TenantId tenantId) {
        TenantScope.requireMatches(tenantId);
    }
}
