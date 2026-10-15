package com.shiyu.ai.education.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import com.shiyu.ai.education.implementation.persistence.dataobject.ExamAttemptQuestionDO;

import org.apache.ibatis.annotations.Mapper;

/** 按租户隔离的不可变考试题目快照映射器。 */
@Mapper
@UseDataSource("agent")
public interface ExamAttemptQuestionMapper extends BaseMapperFlex<ExamAttemptQuestionDO> {}
