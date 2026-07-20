package com.shiyu.ai.dal.model.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.model.dataobject.AiPlatformDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * Ai Platform 接口
 */

/**
 * AI 平台 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface AiPlatformMapper extends BaseMapperFlex<AiPlatformDO> {

}
