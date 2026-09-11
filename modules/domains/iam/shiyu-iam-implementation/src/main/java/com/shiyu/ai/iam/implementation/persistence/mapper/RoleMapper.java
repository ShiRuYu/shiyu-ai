package com.shiyu.ai.iam.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.datasource.DataSourceConfig;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import com.shiyu.ai.iam.implementation.persistence.dataobject.RoleDO;

import org.apache.ibatis.annotations.Mapper;

/** 角色表 数据层 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface RoleMapper extends BaseMapperFlex<RoleDO> {}
