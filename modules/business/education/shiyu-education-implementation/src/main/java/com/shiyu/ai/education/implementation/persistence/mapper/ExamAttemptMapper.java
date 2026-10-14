package com.shiyu.ai.education.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import com.shiyu.ai.education.implementation.persistence.dataobject.ExamAttemptDO;

import org.apache.ibatis.annotations.Mapper;

/** Tenant-scoped exam attempt persistence mapper. */
@Mapper
@UseDataSource("agent")
public interface ExamAttemptMapper extends BaseMapperFlex<ExamAttemptDO> {}
