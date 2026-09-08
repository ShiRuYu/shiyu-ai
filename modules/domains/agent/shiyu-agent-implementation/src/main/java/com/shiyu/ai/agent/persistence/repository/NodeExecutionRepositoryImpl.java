package com.shiyu.ai.agent.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.persistence.dataobject.NodeExecutionDO;
import com.shiyu.ai.agent.persistence.mapper.NodeExecutionMapper;
import com.shiyu.ai.agent.domain.model.NodeExecutionBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NodeExecutionRepositoryImpl implements com.shiyu.ai.agent.port.repository.NodeExecutionRepository {

    @Resource
    private NodeExecutionMapper nodeExecutionMapper;

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
        QueryWrapper query = QueryWrapper.create()
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
        if (tenantId == null || tenantId.value() <= 0) {
            throw new IllegalArgumentException("tenantId is required");
        }
    }
}
