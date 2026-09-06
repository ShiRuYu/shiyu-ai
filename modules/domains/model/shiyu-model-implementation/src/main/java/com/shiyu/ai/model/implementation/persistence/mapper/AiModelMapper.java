package com.shiyu.ai.model.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.model.implementation.persistence.dataobject.AiModelDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 模型 数据层
 */
@Mapper
@UseDataSource("agent")
public interface AiModelMapper extends BaseMapperFlex<AiModelDO> {

}
