package com.shiyu.ai.knowledge.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;

import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeSpaceMemberDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@UseDataSource("agent")
public interface KnowledgeSpaceMemberMapper extends BaseMapperFlex<KnowledgeSpaceMemberDO> {
}

