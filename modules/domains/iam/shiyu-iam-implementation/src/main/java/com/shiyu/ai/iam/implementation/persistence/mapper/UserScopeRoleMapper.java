package com.shiyu.ai.iam.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.mybatis.datasource.DataSourceConfig;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import com.shiyu.ai.iam.implementation.persistence.dataobject.UserScopeRoleDO;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 负责 用户 Scope 角色 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface UserScopeRoleMapper extends BaseMapperFlex<UserScopeRoleDO> {

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param userId 用户标识。
     *
     * @return 符合条件的结果集合。
     */
    default List<UserScopeRoleDO> selectByUserId(Long userId) {
        QueryWrapper qw = QueryWrapper.create().eq(UserScopeRoleDO::getUserId, userId);
        return selectListByQuery(qw);
    }

    /**
     * 查询 用户 Scope 角色 相关业务数据，并返回处理结果。
     *
     * @param userIds 待处理的业务对象标识集合。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    default List<UserScopeRoleDO> selectByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();
        QueryWrapper qw = QueryWrapper.create().in(UserScopeRoleDO::getUserId, userIds);
        return selectListByQuery(qw);
    }
}
