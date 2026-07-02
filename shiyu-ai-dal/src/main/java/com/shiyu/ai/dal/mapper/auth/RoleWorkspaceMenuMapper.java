package com.shiyu.ai.dal.mapper.auth;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.dataobject.auth.RoleWorkspaceMenuDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * Role Workspace Menu 接口
 */

/**
 * 角色工作空间菜单关联表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface RoleWorkspaceMenuMapper extends BaseMapperFlex<RoleWorkspaceMenuDO> {

}
