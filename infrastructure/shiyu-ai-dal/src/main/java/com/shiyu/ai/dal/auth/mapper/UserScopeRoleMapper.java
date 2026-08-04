package com.shiyu.ai.dal.auth.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.auth.dataobject.UserScopeRoleDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
/**
 * User Workspace Role 接口
 */

public interface UserScopeRoleMapper extends BaseMapperFlex<UserScopeRoleDO> {

    default List<UserScopeRoleDO> selectByUserId(Long userId) {
        QueryWrapper qw = QueryWrapper.create()
    /**
     * Eq
     * @return 处理结果
     */
            .eq(UserScopeRoleDO::getUserId, userId);
    /**
     * Select List By Query
     * @return 处理结果
     */
        return selectListByQuery(qw);
    }
}
