package com.shiyu.ai.education.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;

import com.shiyu.ai.education.implementation.persistence.dataobject.QuestionKnowledgeDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@UseDataSource("agent")
public interface QuestionKnowledgeMapper extends BaseMapperFlex<QuestionKnowledgeDO> {
}

