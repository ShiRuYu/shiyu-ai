package com.shiyu.ai.agent.dal.mapper.auth;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.agent.dal.DataSourceConfig;
import com.shiyu.ai.agent.dal.dataobject.auth.RoleDO;
import com.shiyu.ai.agent.dal.dataobject.auth.UserRoleDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户角色关联表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AUTH)
public interface UserRoleMapper extends BaseMapperFlex<UserRoleDO> {

    /**
     * 根据用户ID查询角色列表
     */
    List<RoleDO> selectRolesByUserId(Long userId);
}
