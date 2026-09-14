package com.shiyu.ai.iam.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.mybatis.datasource.DataSourceConfig;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import com.shiyu.ai.iam.implementation.persistence.dataobject.UserScopeRoleDO;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * UserScopeRoleMapper 数据映射接口，负责在身份与访问领域对象与持久化记录之间转换数据。
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
     * 根据条件查询并返回所需数据。
     *
     * @param userIds 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    default List<UserScopeRoleDO> selectByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();
        QueryWrapper qw = QueryWrapper.create().in(UserScopeRoleDO::getUserId, userIds);
        return selectListByQuery(qw);
    }
}
