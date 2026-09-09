package com.shiyu.ai.knowledge.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;

import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeRelationDO;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@UseDataSource("agent")
public interface KnowledgeRelationMapper extends BaseMapperFlex<KnowledgeRelationDO> {
}

