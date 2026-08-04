package com.shiyu.ai.dal.agent.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.agent.dataobject.NodeExecutionDO;
import com.shiyu.ai.dal.agent.mapper.NodeExecutionMapper;
import com.shiyu.ai.agent.domain.model.NodeExecutionBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NodeExecutionRepositoryImpl implements com.shiyu.ai.agent.port.repository.NodeExecutionRepository {

    @Resource
    private NodeExecutionMapper nodeExecutionMapper;

    public void insert(NodeExecutionBO bo) {
        NodeExecutionDO d = MapstructUtils.convert(bo, NodeExecutionDO.class);
        nodeExecutionMapper.insertSelective(d);
        bo.setId(d.getId());
    }

    public void update(NodeExecutionBO bo) {
        NodeExecutionDO d = MapstructUtils.convert(bo, NodeExecutionDO.class);
        nodeExecutionMapper.update(d);
    }

    public List<NodeExecutionBO> selectByExecutionId(String executionId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(NodeExecutionDO::getExecutionId, executionId);
        qw.orderBy(NodeExecutionDO::getStartTime, true);
        List<NodeExecutionDO> doList = nodeExecutionMapper.selectListByQuery(qw);
        return MapstructUtils.convert(doList, NodeExecutionBO.class);
    }
}
