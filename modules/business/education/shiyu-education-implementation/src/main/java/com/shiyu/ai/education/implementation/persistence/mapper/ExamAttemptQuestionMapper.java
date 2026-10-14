package com.shiyu.ai.education.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import com.shiyu.ai.education.implementation.persistence.dataobject.ExamAttemptQuestionDO;

import org.apache.ibatis.annotations.Mapper;

/** Tenant-scoped immutable exam question snapshot mapper. */
@Mapper
@UseDataSource("agent")
public interface ExamAttemptQuestionMapper extends BaseMapperFlex<ExamAttemptQuestionDO> {}
