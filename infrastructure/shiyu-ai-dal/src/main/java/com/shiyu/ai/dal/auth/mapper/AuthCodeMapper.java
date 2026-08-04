package com.shiyu.ai.dal.auth.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.auth.dataobject.AuthCodeDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * Auth Code 接口
 */

/**
 * 认证码表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface AuthCodeMapper extends BaseMapperFlex<AuthCodeDO> {

}
