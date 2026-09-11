package com.shiyu.ai.iam.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.datasource.DataSourceConfig;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import com.shiyu.ai.iam.implementation.persistence.dataobject.MenuDO;

import org.apache.ibatis.annotations.Mapper;

/** 菜单表 数据层 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface MenuMapper extends BaseMapperFlex<MenuDO> {}
