package com.shiyu.ai.dal.agent.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.agent.dataobject.AgentDefDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
/**
 * Agent Def 接口
 */

public interface AgentDefMapper extends BaseMapperFlex<AgentDefDO> {
}
