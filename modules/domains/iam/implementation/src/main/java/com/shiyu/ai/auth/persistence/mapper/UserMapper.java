package com.shiyu.ai.auth.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.datasource.DataSourceConfig;
import com.shiyu.ai.auth.persistence.dataobject.UserDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface UserMapper extends BaseMapperFlex<UserDO> {

}

