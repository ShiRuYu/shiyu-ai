package com.shiyu.ai.dal.mapper.auth;

import com.mybatisflex.annotation.UseDataSource;
import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.dataobject.auth.UserWorkspaceRoleDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface UserWorkspaceRoleMapper extends BaseMapperFlex<UserWorkspaceRoleDO> {

    default List<UserWorkspaceRoleDO> selectByUserId(Long userId) {
        QueryWrapper qw = QueryWrapper.create()
            .eq(UserWorkspaceRoleDO::getUserId, userId);
        return selectListByQuery(qw);
    }
}
