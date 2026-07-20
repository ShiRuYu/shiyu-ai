package com.shiyu.ai.dal.agent.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.agent.dataobject.AgentExecutionDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
/**
 * Agent Execution 接口
 */

public interface AgentExecutionMapper extends BaseMapperFlex<AgentExecutionDO> {
}
