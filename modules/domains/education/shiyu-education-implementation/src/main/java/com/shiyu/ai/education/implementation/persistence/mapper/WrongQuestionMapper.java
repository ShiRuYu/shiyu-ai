package com.shiyu.ai.education.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import com.shiyu.ai.education.implementation.persistence.dataobject.WrongQuestionDO;

import org.apache.ibatis.annotations.Mapper;

@Mapper
@UseDataSource("agent")
public interface WrongQuestionMapper extends BaseMapperFlex<WrongQuestionDO> {}
