package com.shiyu.ai.dal.repository.agent;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.agent.NodeExecutionDO;
import com.shiyu.ai.dal.mapper.agent.NodeExecutionMapper;
import com.shiyu.ai.dal.bo.agent.NodeExecutionBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NodeExecutionRepository {

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
