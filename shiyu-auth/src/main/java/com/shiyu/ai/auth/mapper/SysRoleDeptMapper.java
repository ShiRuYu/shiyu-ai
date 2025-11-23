package com.shiyu.ai.auth.mapper;

import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.auth.domain.SysRoleDeptDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色与部门关联表 数据层
 *
 */
@Mapper
public interface SysRoleDeptMapper extends BaseMapperFlex<SysRoleDeptDO, SysRoleDeptDO> {

}
