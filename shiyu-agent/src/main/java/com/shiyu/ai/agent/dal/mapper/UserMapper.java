package com.shiyu.ai.agent.dal.mapper;

import com.shiyu.ai.agent.dal.dataobject.UserDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户表 数据层
 */
@Mapper
public interface UserMapper extends BaseMapperFlex<UserDO> {

    /**
     * 根据用户名查询用户及其角色信息
     * @param username 用户名
     * @return 用户对象（包含角色列表）
     */
    UserDO selectUserWithRolesByUsername(@Param("username") String username);
}
