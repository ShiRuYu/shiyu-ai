package com.shiyu.ai.agent.dal.mapper.auth;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.agent.dal.DataSourceConfig;
import com.shiyu.ai.agent.dal.dataobject.auth.UserDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AUTH)
public interface UserMapper extends BaseMapperFlex<UserDO> {

    /**
     * 根据用户名查询用户（包含角色信息）
     */
    UserDO selectUserWithRolesByUsername(String username);
}
