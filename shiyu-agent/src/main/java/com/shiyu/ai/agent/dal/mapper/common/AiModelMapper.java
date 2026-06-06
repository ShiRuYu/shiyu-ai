package com.shiyu.ai.agent.dal.mapper.common;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.agent.dal.DataSourceConfig;
import com.shiyu.ai.agent.dal.dataobject.common.AiModelDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 模型 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.COMMON)
public interface AiModelMapper extends BaseMapperFlex<AiModelDO> {

}
