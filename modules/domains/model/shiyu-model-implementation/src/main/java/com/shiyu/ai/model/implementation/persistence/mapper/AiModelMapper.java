package com.shiyu.ai.model.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import com.shiyu.ai.model.implementation.persistence.dataobject.AiModelDO;

import org.apache.ibatis.annotations.Mapper;

/** AI 模型 数据层 */
@Mapper
@UseDataSource("agent")
public interface AiModelMapper extends BaseMapperFlex<AiModelDO> {}
