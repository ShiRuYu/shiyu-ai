package com.shiyu.ai.agent.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.agent.implementation.persistence.dataobject.AgentCheckpointDO;
import com.shiyu.ai.common.mybatis.datasource.DataSourceConfig;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;

import org.apache.ibatis.annotations.Mapper;

/**
 * AgentCheckpointMapper 数据映射接口，负责在智能体领域对象与持久化记录之间转换数据。
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface AgentCheckpointMapper extends BaseMapperFlex<AgentCheckpointDO> {}
