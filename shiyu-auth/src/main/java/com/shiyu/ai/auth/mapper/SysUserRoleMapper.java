package com.shiyu.ai.auth.mapper;

import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.auth.domain.SysUserRoleDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户与角色关联表 数据层
 *
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapperFlex<SysUserRoleDO, SysUserRoleDO> {

}
