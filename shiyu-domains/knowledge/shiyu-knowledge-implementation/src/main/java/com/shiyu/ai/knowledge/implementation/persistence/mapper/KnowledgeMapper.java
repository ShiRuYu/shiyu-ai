package com.shiyu.ai.knowledge.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;

import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@UseDataSource("agent")
public interface KnowledgeMapper extends BaseMapperFlex<KnowledgeDO> {
}

