package com.shiyu.ai.agent.dal.mapper.auth;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.agent.dal.DataSourceConfig;
import com.shiyu.ai.agent.dal.dataobject.auth.AuthCodeDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 认证码表 数据层
 */
@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface AuthCodeMapper extends BaseMapperFlex<AuthCodeDO> {

    /**
     * 根据角色 ID 查询权限码列表
     */
    List<String> selectCodesByRoleId(Long roleId);

    /**
     * 根据用户名查询权限码列表
     */
    List<String> selectCodesByUsername(String username);
    
    /**
     * 根据用户 ID 查询权限码列表
     */
    List<String> selectCodesByUserId(Long userId);
}
