package com.shiyu.ai.demo.mapper;

import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.demo.domain.SysRoleDO;
import com.shiyu.ai.demo.domain.bo.SysRoleBO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色表 数据层
 *
 */
@Mapper
public interface SysRoleMapper extends BaseMapperFlex<SysRoleDO, SysRoleBO> {

}
