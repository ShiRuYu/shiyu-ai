package com.shiyu.ai.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperPlus;
import com.shiyu.ai.demo.domain.SysRoleMenu;

/**
 * 角色与菜单关联表 数据层
 *
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapperPlus<SysRoleMenu, SysRoleMenu> {

}
