package com.shiyu.ai.auth.mapper;

import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.auth.domain.SysRoleWorkspaceDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色与工作空间关联表 数据层
 *
 */
@Mapper
public interface SysRoleWorkspaceMapper extends BaseMapperFlex<SysRoleWorkspaceDO> {

}
