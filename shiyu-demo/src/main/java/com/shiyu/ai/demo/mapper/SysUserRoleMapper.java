package com.shiyu.ai.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.demo.domain.SysUserRoleDO;

import java.util.List;

/**
 * 用户与角色关联表 数据层
 *
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapperFlex<SysUserRoleDO, SysUserRoleDO> {

}
