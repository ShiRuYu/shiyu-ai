package com.shiyu.ai.dal.mapper.education;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.dataobject.education.QuestionKnowledgeDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
/**
 * Question Knowledge 接口
 */

public interface QuestionKnowledgeMapper extends BaseMapperFlex<QuestionKnowledgeDO> {
}
