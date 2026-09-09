package com.shiyu.ai.iam.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.datasource.DataSourceConfig;
import com.shiyu.ai.iam.implementation.persistence.dataobject.AuthCodeDO;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 认证码表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface AuthCodeMapper extends BaseMapperFlex<AuthCodeDO> {

}


