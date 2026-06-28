package com.shiyu.ai.dal.mapper.agent;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.dataobject.agent.AiPlatformDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 平台 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface AiPlatformMapper extends BaseMapperFlex<AiPlatformDO> {

}
