package com.shiyu.ai.auth.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.datasource.DataSourceConfig;
import com.shiyu.ai.auth.persistence.dataobject.RoleScopeMenuDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色作用域菜单关联表数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface RoleScopeMenuMapper extends BaseMapperFlex<RoleScopeMenuDO> {

}

