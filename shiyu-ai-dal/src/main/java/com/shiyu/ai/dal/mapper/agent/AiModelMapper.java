package com.shiyu.ai.dal.mapper.agent;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.dataobject.agent.AiModelDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * Ai Model 接口
 */

/**
 * AI 模型 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface AiModelMapper extends BaseMapperFlex<AiModelDO> {

}
