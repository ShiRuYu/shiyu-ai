package com.shiyu.ai.agent.dal.mapper.auth;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.agent.dal.DataSourceConfig;
import com.shiyu.ai.agent.dal.dataobject.auth.RoleDO;
import com.shiyu.ai.agent.dal.dataobject.auth.UserWorkspaceRoleDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface UserWorkspaceRoleMapper extends BaseMapperFlex<UserWorkspaceRoleDO> {

    List<UserWorkspaceRoleDO> selectByUserId(Long userId);

    /**
     * 根据用户ID查询角色列表（从 user_workspace_role 去重获取）
     */
    List<RoleDO> selectRolesByUserId(Long userId);
}
