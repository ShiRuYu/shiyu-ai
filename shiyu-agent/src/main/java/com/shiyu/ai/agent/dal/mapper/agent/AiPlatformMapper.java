package com.shiyu.ai.agent.dal.mapper.agent;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.agent.dal.DataSourceConfig;
import com.shiyu.ai.agent.dal.dataobject.agent.AiPlatformDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 平台 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface AiPlatformMapper extends BaseMapperFlex<AiPlatformDO> {

}
