package com.shiyu.ai.agent.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.datasource.DataSourceConfig;
import com.shiyu.ai.agent.implementation.persistence.dataobject.ExecutionTimelineDO;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface ExecutionTimelineMapper extends BaseMapperFlex<ExecutionTimelineDO> {
}
