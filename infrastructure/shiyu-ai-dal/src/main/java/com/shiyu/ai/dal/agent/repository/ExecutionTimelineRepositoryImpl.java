package com.shiyu.ai.dal.agent.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.domain.model.ExecutionTimelineBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.agent.dataobject.ExecutionTimelineDO;
import com.shiyu.ai.dal.agent.mapper.ExecutionTimelineMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExecutionTimelineRepositoryImpl implements com.shiyu.ai.agent.port.repository.ExecutionTimelineRepository {

    @Resource
    private ExecutionTimelineMapper executionTimelineMapper;

    public void insert(ExecutionTimelineBO timeline) {
        executionTimelineMapper.insertSelective(MapstructUtils.convert(timeline, ExecutionTimelineDO.class));
    }

    public List<ExecutionTimelineBO> listByExecutionId(String executionId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq("execution_id", executionId);
        qw.orderBy("id", true);
        return MapstructUtils.convert(executionTimelineMapper.selectListByQuery(qw), ExecutionTimelineBO.class);
    }
}
