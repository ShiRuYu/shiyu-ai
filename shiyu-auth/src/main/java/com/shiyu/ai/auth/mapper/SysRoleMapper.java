package com.shiyu.ai.auth.mapper;

import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.auth.domain.SysRoleDO;
import com.shiyu.ai.auth.domain.bo.SysRoleBO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色表 数据层
 *
 */
@Mapper
public interface SysRoleMapper extends BaseMapperFlex<SysRoleDO, SysRoleBO> {

}
