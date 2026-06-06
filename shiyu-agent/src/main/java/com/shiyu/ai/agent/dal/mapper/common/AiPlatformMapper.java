package com.shiyu.ai.agent.dal.mapper.common;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.agent.dal.DataSourceConfig;
import com.shiyu.ai.agent.dal.dataobject.common.AiPlatformDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 平台 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.COMMON)
public interface AiPlatformMapper extends BaseMapperFlex<AiPlatformDO> {

}
