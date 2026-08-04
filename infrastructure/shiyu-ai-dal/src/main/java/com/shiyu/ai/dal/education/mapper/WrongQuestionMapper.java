package com.shiyu.ai.dal.education.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.education.dataobject.WrongQuestionDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
/**
 * Wrong Question 接口
 */

public interface WrongQuestionMapper extends BaseMapperFlex<WrongQuestionDO> {
}
