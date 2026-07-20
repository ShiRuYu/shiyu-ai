package com.shiyu.ai.dal.agent.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.agent.dataobject.ExecutionTimelineDO;
import com.shiyu.ai.dal.agent.mapper.ExecutionTimelineMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExecutionTimelineRepository {

    @Resource
    private ExecutionTimelineMapper executionTimelineMapper;

    public void insert(ExecutionTimelineDO timeline) {
        executionTimelineMapper.insertSelective(timeline);
    }

    public List<ExecutionTimelineDO> listByExecutionId(String executionId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq("execution_id", executionId);
        qw.orderBy("id", true);
        return executionTimelineMapper.selectListByQuery(qw);
    }
}
