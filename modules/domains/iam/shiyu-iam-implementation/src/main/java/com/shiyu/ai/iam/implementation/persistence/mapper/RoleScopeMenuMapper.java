package com.shiyu.ai.iam.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.datasource.DataSourceConfig;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import com.shiyu.ai.iam.implementation.persistence.dataobject.RoleScopeMenuDO;

import org.apache.ibatis.annotations.Mapper;

/**
 * 负责 角色 Scope Menu 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface RoleScopeMenuMapper extends BaseMapperFlex<RoleScopeMenuDO> {}
