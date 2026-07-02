package com.shiyu.ai.dal.mapper.education;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.dataobject.education.ExamQuestionDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
/**
 * Exam Question 接口
 */

public interface ExamQuestionMapper extends BaseMapperFlex<ExamQuestionDO> {
}
