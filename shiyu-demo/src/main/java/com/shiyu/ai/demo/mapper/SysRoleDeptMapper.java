package com.shiyu.ai.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperPlus;
import com.shiyu.ai.demo.domain.SysRoleDept;

/**
 * 角色与部门关联表 数据层
 *
 */
@Mapper
public interface SysRoleDeptMapper extends BaseMapperPlus<SysRoleDept, SysRoleDept> {

}
