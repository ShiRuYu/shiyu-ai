package com.shiyu.ai.agent.dal.mapper.auth;

import com.mybatisflex.annotation.UseDataSource;
import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.DataSourceConfig;
import com.shiyu.ai.agent.dal.dataobject.auth.UserWorkspaceRoleDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface UserWorkspaceRoleMapper extends BaseMapperFlex<UserWorkspaceRoleDO> {

    default List<UserWorkspaceRoleDO> selectByUserId(Long userId) {
        QueryWrapper qw = QueryWrapper.create()
            .eq("userId", userId);
        return selectListByQuery(qw);
    }
}
