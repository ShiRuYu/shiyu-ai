package com.shiyu.ai.agent.dal.mapper.auth;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.agent.dal.DataSourceConfig;
import com.shiyu.ai.agent.dal.dataobject.auth.MenuDO;
import com.shiyu.ai.agent.dal.dataobject.auth.RoleMenuDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 角色菜单关联表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AUTH)
public interface RoleMenuMapper extends BaseMapperFlex<RoleMenuDO> {

    /**
     * 根据角色ID查询菜单列表
     */
    List<MenuDO> selectMenusByRoleId(Long roleId);
}
