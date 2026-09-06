package com.shiyu.ai.auth.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.mybatis.datasource.DataSourceConfig;
import com.shiyu.ai.auth.persistence.dataobject.UserScopeRoleDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface UserScopeRoleMapper extends BaseMapperFlex<UserScopeRoleDO> {

    default List<UserScopeRoleDO> selectByUserId(Long userId) {
        QueryWrapper qw = QueryWrapper.create()
            .eq(UserScopeRoleDO::getUserId, userId);
        return selectListByQuery(qw);
    }

    default List<UserScopeRoleDO> selectByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();
        QueryWrapper qw = QueryWrapper.create().in(UserScopeRoleDO::getUserId, userIds);
        return selectListByQuery(qw);
    }
}

