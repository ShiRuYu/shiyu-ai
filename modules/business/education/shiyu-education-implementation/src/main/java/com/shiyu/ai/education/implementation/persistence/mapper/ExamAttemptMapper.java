package com.shiyu.ai.education.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import com.shiyu.ai.education.implementation.persistence.dataobject.ExamAttemptDO;

import org.apache.ibatis.annotations.Mapper;

/** 按租户隔离的考试作答记录持久化映射器。 */
@Mapper
@UseDataSource("agent")
public interface ExamAttemptMapper extends BaseMapperFlex<ExamAttemptDO> {}
