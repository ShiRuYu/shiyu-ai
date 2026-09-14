package com.shiyu.ai.iam.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.datasource.DataSourceConfig;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import com.shiyu.ai.iam.implementation.persistence.dataobject.RoleScopeAuthCodeDO;

import org.apache.ibatis.annotations.Mapper;

/**
 * RoleScopeAuthCodeMapper 数据映射接口，负责在身份与访问领域对象与持久化记录之间转换数据。
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface RoleScopeAuthCodeMapper extends BaseMapperFlex<RoleScopeAuthCodeDO> {}
