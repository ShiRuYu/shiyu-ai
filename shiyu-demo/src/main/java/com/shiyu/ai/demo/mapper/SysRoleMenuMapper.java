package com.shiyu.ai.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.demo.domain.SysRoleMenuDO;

/**
 * 角色与菜单关联表 数据层
 *
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapperFlex<SysRoleMenuDO, SysRoleMenuDO> {

}
