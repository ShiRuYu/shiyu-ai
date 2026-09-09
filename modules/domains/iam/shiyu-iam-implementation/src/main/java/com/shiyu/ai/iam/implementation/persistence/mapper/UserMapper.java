package com.shiyu.ai.iam.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.datasource.DataSourceConfig;
import com.shiyu.ai.iam.implementation.persistence.dataobject.UserDO;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface UserMapper extends BaseMapperFlex<UserDO> {

}


