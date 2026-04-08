package com.shiyu.ai.agent.dal.mapper;

import com.shiyu.ai.agent.dal.dataobject.AuthCodeDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 权限码表 数据层
 */
@Mapper
public interface AuthCodeMapper extends BaseMapperFlex<AuthCodeDO> {

    /**
     * 根据角色 ID 查询权限码列表
     * @param roleId 角色 ID
     * @return 权限码列表
     */
    List<String> selectCodesByRoleId(Long roleId);

    /**
     * 根据用户名查询权限码列表
     * @param username 用户名
     * @return 权限码列表
     */
    List<String> selectCodesByUsername(String username);
}
