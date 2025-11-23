package com.shiyu.ai.auth.mapper;

import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.auth.domain.SysRoleMenuDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色与菜单关联表 数据层
 *
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapperFlex<SysRoleMenuDO, SysRoleMenuDO> {

}
